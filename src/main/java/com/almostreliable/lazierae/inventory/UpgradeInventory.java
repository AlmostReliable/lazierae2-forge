package com.almostreliable.lazierae.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lib.util.Serializable;

import appeng.core.definitions.AEItems;
import com.google.common.base.Preconditions;

import java.util.List;

public class UpgradeInventory implements Container, Serializable<CompoundTag> {

    private final InventoryHost host;
    private ItemStack upgrades = ItemStack.EMPTY;

    public UpgradeInventory(InventoryHost host) {
        this.host = host;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        if (!upgrades.isEmpty()) tag.put("upgrades", upgrades.serialize());
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        if (tag.contains("upgrades")) upgrades = ItemStack.of(tag.getCompound("upgrades"));
    }

    @Override
    public void clearContent() {
        upgrades = ItemStack.EMPTY;
        host.onUpgradeChange(getUpgradeCount());
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return upgrades.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        Preconditions.checkArgument(slot == 0, "Invalid slot: " + slot);
        return upgrades;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        Preconditions.checkArgument(slot == 0, "Invalid slot: " + slot);

        if (upgrades.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        if (upgrades.getCount() <= amount) {
            result = upgrades;
            upgrades = ItemStack.EMPTY;
        } else {
            result = upgrades.split(amount);
            if (upgrades.isEmpty()) {
                upgrades = ItemStack.EMPTY;
            }
        }

        host.onUpgradeChange(getUpgradeCount());
        setChanged();

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Preconditions.checkArgument(slot == 0, "Invalid slot: " + slot);

        if (upgrades.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = upgrades;
        upgrades = ItemStack.EMPTY;

        host.onUpgradeChange(getUpgradeCount());
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Preconditions.checkArgument(slot == 0, "Invalid slot: " + slot);

        upgrades = stack;
        host.onUpgradeChange(getUpgradeCount());
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 4; // TODO: make configurable
    }

    @Override
    public void setChanged() {
        host.saveChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        Preconditions.checkArgument(slot == 0, "Invalid slot: " + slot);
        return AEItems.SPEED_CARD.isSameAs(stack);
    }

    public void addDrops(List<ItemStack> drops) {
        if (!upgrades.isEmpty()) drops.add(upgrades);
    }

    public int getUpgradeCount() {
        return upgrades.isEmpty() ? 0 : upgrades.getCount();
    }
}
