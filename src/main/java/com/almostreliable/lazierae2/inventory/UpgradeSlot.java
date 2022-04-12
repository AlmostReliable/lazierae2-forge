package com.almostreliable.lazierae2.inventory;

import com.almostreliable.lazierae2.machine.MachineContainer;
import com.almostreliable.lazierae2.machine.MachineTile;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeSlot extends SlotItemHandler {

    private final MachineTile tile;

    public UpgradeSlot(
        MachineContainer parent, IItemHandler itemHandler, int index, int pX, int pY
    ) {
        super(itemHandler, index, pX, pY);
        tile = parent.tile;
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
