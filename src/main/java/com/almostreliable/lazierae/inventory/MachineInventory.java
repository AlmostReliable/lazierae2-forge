package com.almostreliable.lazierae.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lazierae.recipe.CrystalInjectorRecipe;
import com.almostreliable.lib.util.Serializable;

import com.google.common.base.Preconditions;

import java.util.List;

public class MachineInventory implements Container, Serializable<CompoundTag> {

    private final InventoryHost host;
    protected final NonNullList<ItemStack> stacks;

    public MachineInventory(InventoryHost host, int size) {
        this.host = host;
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();

        ListTag stacksTag = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.putInt("slot", i);
                stackTag.put("stack", stack.serialize());
                stacksTag.add(stackTag);
            }
        }
        tag.put("stacks", stacksTag);

        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        ListTag stacksTag = tag.getList("stacks", Tag.TAG_COMPOUND);
        for (int i = 0; i < stacksTag.size(); i++) {
            CompoundTag stackTag = stacksTag.getCompound(i);
            int slot = stackTag.getInt("slot");
            ItemStack stack = ItemStack.of(stackTag.getCompound("stack"));
            stacks.set(slot, stack);
        }
    }

    @Override
    public void clearContent() {
        stacks.clear();
        host.onInventoryChange(-1, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        Preconditions.checkArgument(slot >= 0 && slot < stacks.size(), "Invalid slot: " + slot);
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        Preconditions.checkArgument(slot >= 0 && slot < stacks.size(), "Invalid slot: " + slot);

        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        if (stack.getCount() <= amount) {
            result = stack;
            stacks.set(slot, ItemStack.EMPTY);
        } else {
            result = stack.split(amount);
            if (stack.isEmpty()) {
                stacks.set(slot, ItemStack.EMPTY);
            }
        }

        host.onInventoryChange(slot, stacks.get(slot));
        setChanged();

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Preconditions.checkArgument(slot >= 0 && slot < stacks.size(), "Invalid slot: " + slot);

        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        stacks.set(slot, ItemStack.EMPTY);
        host.onInventoryChange(slot, ItemStack.EMPTY);

        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Preconditions.checkArgument(slot >= 0 && slot < stacks.size(), "Invalid slot: " + slot);

        stacks.set(slot, stack);
        host.onInventoryChange(slot, stack);
        setChanged();
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
    public boolean canPlaceItem(int index, ItemStack stack) {
        return CrystalInjectorRecipe.isInput(stack);
    }

    public void addDrops(List<ItemStack> drops) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) drops.add(stack);
        }
    }
}
