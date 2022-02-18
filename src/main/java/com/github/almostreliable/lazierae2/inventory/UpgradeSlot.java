package com.github.almostreliable.lazierae2.inventory;

import com.github.almostreliable.lazierae2.machine.MachineContainer;
import com.github.almostreliable.lazierae2.machine.MachineTile;
import com.github.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class UpgradeSlot extends SlotItemHandler {

    private final MachineTile tile;

    public UpgradeSlot(
        MachineContainer parent, IItemHandler itemHandler, int index, int pX, int pY
    ) {
        super(itemHandler, index, pX, pY);
        tile = parent.getTile();
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
}
