package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.CraftingEvent;
import appeng.crafting.pattern.AECraftingPattern;
import com.almostreliable.lazierae2.core.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;

public class CraftingQueue implements INBTSerializable<CompoundTag> {

    private final ControllerEntity owner;
    private final int queueSize;
    private final BitSet outputTracker;
    private final Deque<CraftingJob> jobQueue = new ArrayDeque<>();
    private final InternalInventory outputBuffer;

    CraftingQueue(ControllerEntity owner) {
        this.owner = owner;
        queueSize = Config.COMMON.assemblerQueueSize.get();
        outputTracker = new BitSet(queueSize);
        outputBuffer = new InternalInventory(owner, 10, queueSize);
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    void exportOutputs(MEStorage storage, IActionSource source) {
        var changed = false;
        for (var job = outputTracker.nextSetBit(0); job != -1; job = outputTracker.nextSetBit(job + 1)) {
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
                    outputBuffer.setStackInSlot(slot, ItemStack.EMPTY);
                    clearSlots++;
                } else {
                    outputBuffer.getStackInSlot(slot).setCount((int) (sizeBefore - inserted));
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

        jobQueue.offer(job);
    }

    boolean dispatchJob() {
        assert owner.getLevel() != null;

        var job = getCurrentJob();
        if (job == null) return false;

        var output = job.assembleOutput();
        if (output.isEmpty() || !outputBuffer.isEmpty(job.queueIndex)) return false;

        CraftingEvent.fireAutoCraftingEvent(owner.getLevel(), job.pattern, output, job.inputBuffer.toContainer());
        job.fillOutputBuffer();

        outputTracker.set(job.queueIndex);
        jobQueue.pop();
        return true;
    }

    boolean canExport() {
        return !outputTracker.isEmpty();
    }

    @Nullable
    CraftingJob getCurrentJob() {
        return jobQueue.peek();
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

    private final class CraftingJob {

        private final int queueIndex;
        private final AECraftingPattern pattern;
        private final InternalInventory inputBuffer;
        @Nullable private ItemStack output;

        private CraftingJob(int queueIndex, AECraftingPattern pattern) {
            this.queueIndex = queueIndex;
            this.pattern = pattern;
            inputBuffer = new InternalInventory(owner, 3, 3);
        }

        private ItemStack assembleOutput() {
            if (output == null) {
                output = pattern.getOutput(inputBuffer.toContainer(), owner.getLevel());
            }
            return output;
        }

        private void fillOutputBuffer() {
            var remainders = pattern.getRemainingItems(inputBuffer.toCraftingContainer());
            for (var slot = 0; slot < outputBuffer.getWidth() - 1; slot++) {
                if (remainders.get(slot).isEmpty()) continue;
                outputBuffer.setStackInSlot(queueIndex, slot, remainders.get(slot));
            }
            outputBuffer.setStackInSlot(queueIndex, 9, assembleOutput());
        }
    }
}
