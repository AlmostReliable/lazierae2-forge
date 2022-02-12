package com.github.almostreliable.lazierae2.component;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class InventoryHandler extends ItemStackHandler {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final TileEntity tile;

    public InventoryHandler(TileEntity tile, int inputSlots) {
        super(inputSlots + NON_INPUT_SLOTS);
        this.tile = tile;
    }

    public void setSizeByInputs(int inputs) {
        setSize(inputs + NON_INPUT_SLOTS);
    }

    public int getInputSlots() {
        return getSlots() - NON_INPUT_SLOTS;
    }

    @Nonnull
    public ItemStack getStackInOutput() {
        return getStackInSlot(OUTPUT_SLOT);
    }

    public void setStackInOutput(ItemStack stack) {
        setStackInSlot(OUTPUT_SLOT, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        tile.setChanged();
    }
}
