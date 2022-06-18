package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.content.GenericInventory;
import com.almostreliable.lazierae2.content.assembler.PatternHolderBlock;
import com.almostreliable.lazierae2.content.assembler.PatternHolderBlock.HOLDER_TIER;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.almostreliable.lazierae2.core.Constants.Nbt.ACCELERATORS_ID;
import static com.almostreliable.lazierae2.core.Constants.Nbt.INVALID_ROWS_ID;

public class ControllerData extends GenericInventory<ControllerEntity> {

    public final Set<Integer> invalidRows = new HashSet<>();
    final List<IPatternDetails> patterns = new ArrayList<>();
    private int accelerators;

    ControllerData(ControllerEntity controller) {
        super(controller);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putIntArray(INVALID_ROWS_ID, invalidRows.stream().mapToInt(Integer::intValue).toArray());
        tag.putInt(ACCELERATORS_ID, accelerators);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains(INVALID_ROWS_ID)) {
            var invalidRowsTag = tag.getIntArray(INVALID_ROWS_ID);
            for (var invalidRow : invalidRowsTag) {
                invalidRows.add(invalidRow);
            }
        }
        if (tag.contains(ACCELERATORS_ID)) accelerators = tag.getInt(ACCELERATORS_ID);
    }

    @Override
    protected void onContentsChanged() {
        if (owner.getLevel() != null && !owner.getLevel().isClientSide) {
            updatePatterns();
        }
        super.onContentsChanged();
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        validateSlot(slot);
        return AEItems.CRAFTING_PATTERN.isSameAs(stack);
    }

    @Override
    protected void setSize(int size) {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Size must be a multiple of 9");
        }
        invalidRows.clear();
        var oldSize = getSlots();
        if (size < oldSize) {
            for (var slot = size; slot < oldSize; slot++) {
                if (getStackInSlot(slot).isEmpty()) continue;
                for (var row = size / 9; row < oldSize / 9; row++) {
                    invalidRows.add(row);
                }
                break;
            }
            if (!invalidRows.isEmpty()) return;
        }
        setSize(size, true);
    }

    void validateSize() {
        assert owner.getLevel() != null;
        if (invalidRows.isEmpty()) return;
        for (var row : invalidRows) {
            for (var slot = 0; slot < 9; slot++) {
                if (!getStackInSlot(row * 9 + slot).isEmpty()) return;
            }
        }
        var rows = invalidRows.size();
        invalidRows.clear();
        setSize(getSlots() - rows * 9, true);
        updatePatterns();
        owner.getLevel().sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), 1 | 2);
    }

    void updatePatterns() {
        patterns.clear();
        for (var row = 0; row < getSlots() / 9; row++) {
            if (invalidRows.contains(row)) continue;
            for (var slot = 0; slot < 9; slot++) {
                if (getStackInSlot(row * 9 + slot).isEmpty()) continue;
                var details = PatternDetailsHelper.decodePattern(getStackInSlot(row * 9 + slot), owner.getLevel());
                if (details == null) continue;
                patterns.add(details);
            }
        }
        ICraftingProvider.requestUpdate(owner.getMainNode());
    }

    int getAccelerators() {
        return accelerators;
    }

    void setHolders(List<? extends PatternHolderBlock> holders) {
        accelerators = 0;
        var size = 0;
        for (var holder : holders) {
            if (holder.getTier() == HOLDER_TIER.ACCELERATOR) {
                accelerators++;
                continue;
            }
            size += holder.getTier().ordinal() * 9;
        }
        setSize(size);
    }
}
