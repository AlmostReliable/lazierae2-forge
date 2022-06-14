package com.almostreliable.lazierae2.inventory;

import appeng.core.definitions.AEItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class PatternSlot extends SlotItemHandler {

    public PatternSlot(IItemHandlerModifiable itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && AEItems.CRAFTING_PATTERN.isSameAs(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
