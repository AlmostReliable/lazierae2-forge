package com.almostreliable.lazierae2.inventory;

import com.almostreliable.lazierae2.content.processor.ProcessorEntity;
import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeSlot extends SlotItemHandler {

    private final ProcessorEntity entity;

    public UpgradeSlot(ProcessorMenu parent, IItemHandler itemHandler, int index, int pX, int pY) {
        super(itemHandler, index, pX, pY);
        entity = parent.entity;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        entity.recalculateEnergyCapacity();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return GameUtil.isValidUpgrade(stack);
    }

    @Override
    public int getMaxStackSize() {
        return entity.getProcessorType().getUpgradeSlots();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return getMaxStackSize();
    }
}
