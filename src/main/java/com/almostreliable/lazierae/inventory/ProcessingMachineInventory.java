package com.almostreliable.lazierae.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

import com.almostreliable.lazierae.util.Utils;
import com.almostreliable.lib.block.io.SideConfiguration;

import org.jetbrains.annotations.Nullable;

public class ProcessingMachineInventory extends MachineInventory implements WorldlyContainer {

    private final SidedInventoryHost host;

    public ProcessingMachineInventory(SidedInventoryHost host, int size) {
        super(host, size);
        this.host = host;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return Utils.ArrayFromTo(0, stacks.size());
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        SideConfiguration sideConfig = host.getSideConfiguration();
        return direction == null || sideConfig.get(direction).isInput();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        SideConfiguration sideConfig = host.getSideConfiguration();
        return sideConfig.get(direction).isOutput();
    }
}
