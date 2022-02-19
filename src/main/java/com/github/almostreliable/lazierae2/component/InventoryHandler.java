package com.github.almostreliable.lazierae2.component;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InventoryHandler extends ItemStackHandler {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final TileEntity tile;
    private IInventory iinventory;
    private boolean changed;

    public InventoryHandler(TileEntity tile, int inputSlots) {
        super(inputSlots + NON_INPUT_SLOTS);
        this.tile = tile;
    }

    public void shrinkInputSlots() {
        for (int i = NON_INPUT_SLOTS; i < getSlots(); i++) {
            if (getStackInSlot(i).isEmpty()) continue;
            if (getStackInSlot(i).getCount() == 1) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                getStackInSlot(i).shrink(1);
            }
        }
    }

    public IInventory asIInventory() {
        if (iinventory == null || changed) {
            iinventory = new RecipeWrapper(this);
            changed = false;
        }
        return iinventory;
    }

    @Override
    protected void onContentsChanged(int slot) {
        changed = true;
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
