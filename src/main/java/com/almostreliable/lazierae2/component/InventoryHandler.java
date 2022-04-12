package com.almostreliable.lazierae2.component;

import com.almostreliable.lazierae2.machine.MachineEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InventoryHandler extends ItemStackHandler {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final MachineEntity entity;
    private Container vanillaInventory;
    private boolean changed;

    public InventoryHandler(MachineEntity entity) {
        super(entity.getMachineType().getInputSlots() + NON_INPUT_SLOTS);
        this.entity = entity;
    }

    public void dropContents() {
        if (entity.getLevel() == null) return;
        var pos = entity.getBlockPos();
        for (var i = OUTPUT_SLOT; i < getSlots(); i++) {
            var stack = getStackInSlot(i);
            if (stack.isEmpty()) continue;
            entity
                .getLevel()
                .addFreshEntity(new ItemEntity(entity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    public CompoundTag serializeUpgrades() {
        return getStackInSlot(UPGRADE_SLOT).save(new CompoundTag());
    }

    public void deserializeUpgrades(CompoundTag tag) {
        setStackInSlot(UPGRADE_SLOT, ItemStack.of(tag));
    }

    public void shrinkInputSlots() {
        for (var i = NON_INPUT_SLOTS; i < getSlots(); i++) {
            if (getStackInSlot(i).isEmpty()) continue;
            if (getStackInSlot(i).getCount() == 1) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                getStackInSlot(i).shrink(1);
                entity.setChanged();
            }
        }
    }

    public Container toVanilla() {
        if (vanillaInventory == null || changed) {
            vanillaInventory = new RecipeWrapper(this);
            changed = false;
        }
        return vanillaInventory;
    }

    @Override
    protected void onContentsChanged(int slot) {
        changed = true;
        entity.setChanged();
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
