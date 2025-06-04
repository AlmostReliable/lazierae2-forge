package com.almostreliable.lazierae.inventory;

import net.minecraft.world.item.ItemStack;

public interface InventoryHost {

    void saveChanges();

    /**
     * Called when the stack in the given slot changes.
     *
     * @param slot  The slot that changed.
     * @param stack The new stack in the slot.
     */
    void onInventoryChange(int slot, ItemStack stack);

    /**
     * Called when the upgrade stack in the inventory changes.
     *
     * @param amount The new amount of upgrades in the inventory.
     */
    void onUpgradeChange(int amount);
}
