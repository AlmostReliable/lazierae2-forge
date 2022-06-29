package com.almostreliable.lazierae2.content.assembler.controller;

import appeng.menu.AutoCraftingMenu;
import com.almostreliable.lazierae2.content.GenericInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

class InternalInventory extends GenericInventory<ControllerEntity> {

    private final int width;
    private final CraftingContainer craftingContainer;

    InternalInventory(ControllerEntity owner, int width, int height) {
        super(owner, width * height);
        this.width = width;
        craftingContainer = new CraftingContainerWrapper(width, height);
    }

    @Override
    protected void onContentsChanged() {
        owner.saveChanges();
    }

    void setStackInSlot(int row, int slot, ItemStack stack) {
        setStackInSlot(row * width + slot, stack);
    }

    ItemStack getStackInSlot(int row, int slot) {
        return getStackInSlot(row * width + slot);
    }

    boolean isEmpty(int row) {
        for (var slot = 0; slot < width; slot++) {
            if (!getStackInSlot(row * width + slot).isEmpty()) return false;
        }
        return true;
    }

    CraftingContainer toCraftingContainer() {
        return craftingContainer;
    }

    int getWidth() {
        return width;
    }

    private final class CraftingContainerWrapper extends CraftingContainer {

        private CraftingContainerWrapper(int width, int height) {
            super(new AutoCraftingMenu(), width, height);
        }

        @Override
        public int getContainerSize() {
            return toContainer().getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return toContainer().isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return toContainer().getItem(index);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return toContainer().removeItemNoUpdate(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return toContainer().removeItem(index, count);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            toContainer().setItem(index, stack);
        }

        @Override
        public void setChanged() {
            toContainer().setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return toContainer().stillValid(player);
        }

        @Override
        public void clearContent() {
            toContainer().clearContent();
        }

        @Override
        public void fillStackedContents(StackedContents helper) {
            for (var slot = 0; slot < getSlots(); slot++) {
                helper.accountSimpleStack(getStackInSlot(slot));
            }
        }

        @Override
        public int getMaxStackSize() {
            return toContainer().getMaxStackSize();
        }

        @Override
        public void startOpen(Player player) {
            toContainer().startOpen(player);
        }

        @Override
        public void stopOpen(Player player) {
            toContainer().stopOpen(player);
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return toContainer().canPlaceItem(index, stack);
        }

        @Override
        public int countItem(Item item) {
            return toContainer().countItem(item);
        }

        @Override
        public boolean hasAnyOf(Set<Item> set) {
            return toContainer().hasAnyOf(set);
        }
    }
}
