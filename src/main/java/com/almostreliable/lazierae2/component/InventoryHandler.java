package com.almostreliable.lazierae2.component;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InventoryHandler extends ItemStackHandler {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final TileEntity tile;
    private IInventory vanillaInventory;
    private boolean changed;

    public InventoryHandler(TileEntity tile, int inputSlots) {
        super(inputSlots + NON_INPUT_SLOTS);
        this.tile = tile;
    }

    public void dropContents() {
        if (tile.getLevel() == null) return;
        BlockPos pos = tile.getBlockPos();
        for (int i = OUTPUT_SLOT; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack.isEmpty()) continue;
            tile.getLevel().addFreshEntity(new ItemEntity(tile.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    public CompoundNBT serializeUpgrades() {
        return getStackInSlot(UPGRADE_SLOT).save(new CompoundNBT());
    }

    public void deserializeUpgrades(CompoundNBT nbt) {
        setStackInSlot(UPGRADE_SLOT, ItemStack.of(nbt));
    }

    public void shrinkInputSlots() {
        for (int i = NON_INPUT_SLOTS; i < getSlots(); i++) {
            if (getStackInSlot(i).isEmpty()) continue;
            if (getStackInSlot(i).getCount() == 1) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                getStackInSlot(i).shrink(1);
                tile.setChanged();
            }
        }
    }

    public IInventory toVanilla() {
        if (vanillaInventory == null || changed) {
            vanillaInventory = new RecipeWrapper(this);
            changed = false;
        }
        return vanillaInventory;
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
