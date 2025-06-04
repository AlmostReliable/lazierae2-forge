package com.almostreliable.lazierae.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.google.common.base.Preconditions;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Utility class for holding multiple containers in one.
 */
public class CombinedContainer implements Container {

    private final Container[] containers;
    private final int[] indexes;
    private final int size;

    public CombinedContainer(Container... containers) {
        this.containers = containers;
        this.indexes = new int[containers.length];
        int s = 0;
        for (int i = 0; i < containers.length; i++) {
            s += containers[i].getContainerSize();
            indexes[i] = s;
        }
        this.size = s;
    }

    @Override
    public void clearContent() {
        for (Container container : containers) {
            container.clearContent();
        }
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Container container : containers) {
            if (!container.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int index = getIndexForSlot(slot);
        Container container = getContainerFromIndex(index);
        int targetSlot = getSlotFromIndex(slot, index);
        return container.getItem(targetSlot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        int index = getIndexForSlot(slot);
        Container container = getContainerFromIndex(index);
        int targetSlot = getSlotFromIndex(slot, index);
        return container.removeItem(targetSlot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        int index = getIndexForSlot(slot);
        Container container = getContainerFromIndex(index);
        int targetSlot = getSlotFromIndex(slot, index);
        return container.removeItemNoUpdate(targetSlot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int index = getIndexForSlot(slot);
        Container container = getContainerFromIndex(index);
        int targetSlot = getSlotFromIndex(slot, index);
        container.setItem(targetSlot, stack);
    }

    @Override
    public void setChanged() {
        for (Container container : containers) {
            container.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        for (Container container : containers) {
            if (!container.stillValid(player)) return false;
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        int slot = getIndexForSlot(index);
        Container container = getContainerFromIndex(slot);
        int targetSlot = getSlotFromIndex(index, slot);
        return container.canPlaceItem(targetSlot, stack);
    }

    @Override
    public int countItem(Item item) {
        int count = 0;
        for (Container container : containers) {
            count += container.countItem(item);
        }
        return count;
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        for (Container container : containers) {
            if (container.hasAnyOf(set)) return true;
        }
        return false;
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        for (Container container : containers) {
            if (container.hasAnyMatching(predicate)) return true;
        }
        return false;
    }

    private int getIndexForSlot(int slot) {
        Preconditions.checkArgument(slot >= 0 && slot < size, "Slot out of bounds");

        for (int i = 0; i < indexes.length; i++) {
            if (slot - indexes[i] < 0) {
                return i;
            }
        }

        throw new IllegalStateException("Slot out of bounds");
    }

    private int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= indexes.length) {
            return slot;
        }
        return slot - indexes[index - 1];
    }

    private Container getContainerFromIndex(int index) {
        Preconditions.checkArgument(index >= 0 && index < containers.length, "Index out of bounds");
        return containers[index];
    }
}
