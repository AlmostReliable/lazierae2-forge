package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.CraftingEvent;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.CraftingPatternItem;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.core.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class CraftingQueue implements INBTSerializable<CompoundTag> {

    private final ControllerEntity owner;
    private final int queueSize;
    private final Deque<CraftingJob> jobQueue = new ArrayDeque<>();
    private final InternalInventory outputBuffer;
    private BitSet outputTracker;

    CraftingQueue(ControllerEntity owner) {
        this.owner = owner;
        queueSize = Config.COMMON.assemblerQueueSize.get();
        outputTracker = new BitSet(queueSize);
        outputBuffer = new InternalInventory(owner, 10, queueSize);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var queue = new ListTag();
        for (var job : jobQueue) {
            queue.add(job.serializeNBT());
        }
        tag.put(JOB_QUEUE_ID, queue);
        tag.put(OUTPUT_BUFFER_ID, outputBuffer.serializeNBT());
        tag.putByteArray(OUTPUT_TRACKER_ID, outputTracker.toByteArray());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        jobQueue.clear();
        for (var jobTagRaw : tag.getList(JOB_QUEUE_ID, Tag.TAG_COMPOUND)) {
            var jobTag = (CompoundTag) jobTagRaw;
            var outputPos = jobTag.getInt(OUTPUT_POS_ID);
            var stack = ItemStack.of(jobTag.getCompound(PATTERN_ID));
            if (!stack.isEmpty() && stack.getItem() instanceof CraftingPatternItem) {
                var job = new CraftingJob(outputPos, stack);
                job.deserializeNBT(jobTag);
                jobQueue.add(job);
            }
        }
        outputBuffer.deserializeNBT(tag.getCompound(OUTPUT_BUFFER_ID));
        outputTracker = BitSet.valueOf(tag.getByteArray(OUTPUT_TRACKER_ID));
    }

    void exportOutputs(MEStorage storage, IActionSource source) {
        var changed = false;
        for (var job = outputTracker.nextSetBit(0); job != -1; job = outputTracker.nextSetBit(job + 1)) {
            if (outputBuffer.isEmpty(job)) continue;
            var clearSlots = 0;
            for (var slot = 0; slot < outputBuffer.getWidth(); slot++) {
                var bufferStack = outputBuffer.getStackInSlot(job, slot);
                var stack = GenericStack.fromItemStack(bufferStack);
                if (stack == null) {
                    clearSlots++;
                    continue;
                }
                var sizeBefore = stack.amount();
                var inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, source);
                if (inserted == 0) continue;
                if (inserted >= sizeBefore) {
                    outputBuffer.setStackInSlot(job, slot, ItemStack.EMPTY);
                    clearSlots++;
                } else {
                    outputBuffer.getStackInSlot(job, slot).setCount((int) (sizeBefore - inserted));
                }
                changed = true;
            }
            if (clearSlots == outputBuffer.getWidth()) {
                outputTracker.clear(job);
            }
        }

        if (changed) {
            owner.saveChanges();
        }
    }

    // substitution logic taken from MolecularAssemblerBlockEntity
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    void pushJob(AECraftingPattern pattern, KeyCounter[] inputHolder) {
        var job = new CraftingJob(outputTracker.nextClearBit(0), pattern);

        for (var slot = 0; slot < job.inputBuffer.getSlots(); slot++) {
            var inputId = pattern.getCompressedIndexFromSparse(slot);
            if (inputId == -1) continue;
            var list = inputHolder[inputId];

            // fluid substitution
            var validFluid = pattern.getValidFluid(slot);
            if (validFluid != null) {
                var validFluidKey = validFluid.what();
                var amount = list.get(validFluidKey);
                var requiredAmount = (int) validFluid.amount();
                if (amount >= requiredAmount) {
                    job.inputBuffer.setStackInSlot(
                        slot,
                        GenericStack.wrapInItemStack(validFluidKey, requiredAmount)
                    );
                    list.remove(validFluidKey, requiredAmount);
                    continue;
                }
            }

            // item substitution
            for (var entry : list) {
                if (entry.getLongValue() > 0 && entry.getKey() instanceof AEItemKey itemKey) {
                    job.inputBuffer.setStackInSlot(slot, itemKey.toStack());
                    list.remove(itemKey, 1);
                    break;
                }
            }
        }

        // sanity check
        for (var list : inputHolder) {
            list.removeZeros();
            if (!list.isEmpty()) {
                throw new IllegalStateException(
                    "Could not fill assembler with some items, including " + list.iterator().next()
                );
            }
        }

        outputTracker.set(job.outputPos);
        jobQueue.offer(job);
    }

    boolean dispatchJob() {
        assert owner.getLevel() != null;

        var job = jobQueue.peek();
        if (job == null) return false;

        var output = job.getOutput();
        if (output.isEmpty() || !outputBuffer.isEmpty(job.outputPos)) return false;

        CraftingEvent.fireAutoCraftingEvent(owner.getLevel(), job.getPattern(), output, job.inputBuffer.toContainer());
        job.fillOutputBuffer();

        jobQueue.pop();
        return true;
    }

    boolean canExport() {
        if (outputTracker.isEmpty()) return false;
        for (var job = outputTracker.nextSetBit(0); job != -1; job = outputTracker.nextSetBit(job + 1)) {
            if (!outputBuffer.isEmpty(job)) return true;
        }
        return false;
    }

    ItemStack[] getCraftingMatrix() {
        var matrix = new ItemStack[10];
        Arrays.fill(matrix, ItemStack.EMPTY);
        var job = jobQueue.peek();
        if (job == null) return matrix;
        for (var slot = 0; slot < job.inputBuffer.getSlots(); slot++) {
            matrix[slot] = job.inputBuffer.getStackInSlot(slot);
        }
        matrix[9] = job.getOutput();
        return matrix;
    }

    boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    int getJobAmount() {
        return jobQueue.size();
    }

    boolean isFull() {
        return jobQueue.size() >= queueSize;
    }

    private final class CraftingJob implements INBTSerializable<CompoundTag> {

        private final InternalInventory inputBuffer;
        private final int outputPos;
        @Nullable private ItemStack output;
        @Nullable private AECraftingPattern pattern;
        @Nullable private ItemStack patternStack;

        private CraftingJob(int outputPos, AECraftingPattern pattern) {
            this(outputPos);
            this.pattern = pattern;
        }

        private CraftingJob(int outputPos, ItemStack patternStack) {
            this(outputPos);
            this.patternStack = patternStack;
        }

        private CraftingJob(int outputPos) {
            inputBuffer = new InternalInventory(owner, 3, 3);
            this.outputPos = outputPos;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putInt(OUTPUT_POS_ID, outputPos);
            tag.put(PATTERN_ID, getPattern().getDefinition().toStack().serializeNBT());
            tag.put(INPUT_BUFFER_ID, inputBuffer.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            inputBuffer.deserializeNBT(tag.getCompound(INPUT_BUFFER_ID));
        }

        private void fillOutputBuffer() {
            var remainders = getPattern().getRemainingItems(inputBuffer.toCraftingContainer());
            for (var slot = 0; slot < outputBuffer.getWidth() - 1; slot++) {
                if (remainders.get(slot).isEmpty()) continue;
                outputBuffer.setStackInSlot(outputPos, slot, remainders.get(slot));
            }
            outputBuffer.setStackInSlot(outputPos, 9, getOutput());
        }

        // TODO: check if this should really be cached
        private ItemStack getOutput() {
            if (output == null) {
                output = getPattern().getOutput(inputBuffer.toContainer(), owner.getLevel());
            }
            return output;
        }

        private AECraftingPattern getPattern() {
            if (pattern == null) {
                if (patternStack == null || patternStack.isEmpty() || !(patternStack.getItem() instanceof CraftingPatternItem item)) {
                    throw new IllegalStateException("Pattern item is invalid!");
                }
                pattern = item.decode(patternStack, owner.getLevel(), false);
                if (pattern == null) {
                    LazierAE2.LOG.warn("Unable to restore auto-crafting pattern after load: {}", patternStack.getTag());
                    outputTracker.clear(outputPos);
                    jobQueue.remove(this);
                }
            }
            return pattern;
        }
    }
}
