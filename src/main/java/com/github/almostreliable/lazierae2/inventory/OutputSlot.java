package com.github.almostreliable.lazierae2.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class OutputSlot extends SlotItemHandler {

    public OutputSlot(IItemHandler itemHandler, int index, int pX, int pY) {
        super(itemHandler, index, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
