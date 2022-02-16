package com.github.almostreliable.lazierae2.component;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryHandler extends ItemStackHandler {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final TileEntity tile;

    public InventoryHandler(TileEntity tile, int inputSlots) {
        super(inputSlots + NON_INPUT_SLOTS);
        this.tile = tile;
    }

    public void shrinkInputSlots() {
        for (int i = NON_INPUT_SLOTS; i < getSlots(); i++) {
            if (getStackInSlot(i).isEmpty()) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                getStackInSlot(i).shrink(1);
            }
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        tile.setChanged();
    }

    public int getInputSlots() {
        return getSlots() - NON_INPUT_SLOTS;
    }

    public ItemStack getStackInOutput() {
        return getStackInSlot(OUTPUT_SLOT);
    }

    public void setStackInOutput(ItemStack stack) {
        setStackInSlot(OUTPUT_SLOT, stack);
    }

    public int getUpgradeCount() {
        return getStackInSlot(UPGRADE_SLOT).getCount();
    }
}
