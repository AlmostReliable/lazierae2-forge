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
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Nbt.ACCELERATORS_ID;

public class ControllerData extends GenericInventory<ControllerEntity> {

    // TODO: see if this needs serialization
    private final List<Integer> invalidRowIndexes = new ArrayList<>();
    private final List<IPatternDetails> patterns = new ArrayList<>();
    private int accelerators;

    ControllerData(ControllerEntity controller) {
        super(controller);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putInt(ACCELERATORS_ID, accelerators);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
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
        var oldSize = getSlots();
        if (size < oldSize) {
            for (var slot = size; slot < oldSize; slot++) {
                if (getStackInSlot(slot).isEmpty()) continue;
                for (var row = size / 9 + 1; row <= oldSize / 9; row += 9) {
                    invalidRowIndexes.add(row);
                }
                break;
            }
            if (!invalidRowIndexes.isEmpty()) return;
        }
        invalidRowIndexes.clear();
        setSize(size, true);
    }

    void updatePatterns() {
        patterns.clear();
        for (var slot = 0; slot < getSlots(); slot++) {
            if (getStackInSlot(slot).isEmpty()) continue;
            var details = PatternDetailsHelper.decodePattern(getStackInSlot(slot), owner.getLevel());
            if (details == null) continue;
            patterns.add(details);
        }
        ICraftingProvider.requestUpdate(owner.getMainNode());
    }

    public int getAccelerators() {
        return accelerators;
    }

    public List<Integer> getInvalidRowIndexes() {
        return invalidRowIndexes;
    }

    List<IPatternDetails> getPatterns() {
        return patterns;
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
