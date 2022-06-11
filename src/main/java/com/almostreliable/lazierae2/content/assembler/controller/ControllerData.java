package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.crafting.IPatternDetails;
import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.content.assembler.holder.PatternHolderBlock;
import com.almostreliable.lazierae2.content.assembler.holder.PatternHolderEntity;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ControllerData implements INBTSerializable<ListTag> {

    private final ControllerEntity controller;
    private final Set<BlockPos> holderPositions = new HashSet<>();
    private final PatternInventoryWrapper patternStorage;
    private int accelerators;

    ControllerData(ControllerEntity controller) {
        this.controller = controller;
        patternStorage = new PatternInventoryWrapper();
    }

    @Override
    public ListTag serializeNBT() {
        var tag = new ListTag();
        for (var holderPos : holderPositions) {
            tag.add(NbtUtils.writeBlockPos(holderPos));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(ListTag tag) {
        holderPositions.clear();
        for (var holderPos : tag) {
            holderPositions.add(NbtUtils.readBlockPos((CompoundTag) holderPos));
        }
    }

    public void reset() {
        holderPositions.clear();
        patternStorage.clear();
        accelerators = 0;
    }

    PatternHolderEntity getHolder(int index) {
        return patternStorage.getHolders().get(index);
    }

    void addHolderPos(BlockPos holderPos) {
        holderPositions.add(holderPos);
    }

    void initialize() {
        if (controller.getLevel() == null || holderPositions.isEmpty()) return;
        for (var holderPos : holderPositions) {
            var block = controller.getLevel().getBlockState(holderPos).getBlock();
            holderPositions.removeIf(pos -> !(block instanceof PatternHolderBlock));
            var holder = controller.getLevel().getBlockEntity(holderPos);
            if (holder instanceof PatternHolderEntity holderEntity) {
                holderEntity.setController(controller);
                patternStorage.addHolder(holderEntity);
            } else {
                accelerators++;
            }
        }
    }

    int getHolderCount() {
        return holderPositions.size();
    }

    public int getAccelerators() {
        return accelerators;
    }

    PatternInventoryWrapper getPatternStorage() {
        return patternStorage;
    }

    List<IPatternDetails> getPatterns() {
        List<IPatternDetails> patterns = new ArrayList<>();
        for (var holder : patternStorage.getHolders()) {
            holder.updatePatterns(patterns);
        }
        return patterns;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class PatternInventoryWrapper implements IItemHandlerModifiable {

        private final RangeMap<Integer, PatternHolderEntity> patternHolders = TreeRangeMap.create();
        private int size;

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (patternHolders.get(slot) == null) return;
            Objects.requireNonNull(patternHolders.get(slot)).patternStorage.setStackInSlot(
                size - slot - 1,
                stack
            );
        }

        public void clear() {
            patternHolders.clear();
            size = 0;
        }

        private void addHolder(PatternHolderEntity holder) {
            var upper = holder.patternStorage.getSlots() + size - 1;
            patternHolders.put(Range.closed(size, upper), holder);
            size = upper + 1;
        }

        private List<PatternHolderEntity> getHolders() {
            return new ArrayList<>(patternHolders.asMapOfRanges().values());
        }

        @Override
        public int getSlots() {
            return size;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (patternHolders.get(slot) == null) return ItemStack.EMPTY;
            return Objects.requireNonNull(patternHolders.get(slot)).patternStorage.getStackInSlot(size - slot - 1);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (patternHolders.get(slot) == null) return stack;
            return Objects.requireNonNull(patternHolders.get(slot)).patternStorage.insertItem(
                size - slot - 1,
                stack,
                simulate
            );
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (patternHolders.get(slot) == null) return ItemStack.EMPTY;
            return Objects.requireNonNull(patternHolders.get(slot)).patternStorage.extractItem(
                size - slot - 1,
                amount,
                simulate
            );
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return AEItems.CRAFTING_PATTERN.isSameAs(stack);
        }
    }
}
