package com.almostreliable.lazierae.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lazierae.mixin.SlotAccessor;

public class ContainerFilteredSlot extends Slot {

    public ContainerFilteredSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @SuppressWarnings("CastToIncompatibleInterface")
    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(((SlotAccessor) this).getSlot(), stack);
    }
}
