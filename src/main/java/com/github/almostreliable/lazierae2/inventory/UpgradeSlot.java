package com.github.almostreliable.lazierae2.inventory;

import com.github.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UpgradeSlot extends SlotItemHandler {

    public UpgradeSlot(IItemHandler itemHandler, int index, int pX, int pY) {
        super(itemHandler, index, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return GameUtil.isUpgrade(stack);
    }

    @Override
    public int getMaxStackSize() {
        // TODO: make configurable
        return 8;
    }
}
