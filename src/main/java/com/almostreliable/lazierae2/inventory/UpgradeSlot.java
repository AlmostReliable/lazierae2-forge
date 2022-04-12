package com.almostreliable.lazierae2.inventory;

import com.almostreliable.lazierae2.machine.MachineContainer;
import com.almostreliable.lazierae2.machine.MachineEntity;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeSlot extends SlotItemHandler {

    private final MachineEntity tile;

    public UpgradeSlot(
        MachineContainer parent, IItemHandler itemHandler, int index, int pX, int pY
    ) {
        super(itemHandler, index, pX, pY);
        tile = parent.entity;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        tile.recalculateEnergyCapacity();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return GameUtil.isValidUpgrade(stack);
    }

    @Override
    public int getMaxStackSize() {
        return tile.getMachineType().getUpgradeSlots();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return getMaxStackSize();
    }
}
