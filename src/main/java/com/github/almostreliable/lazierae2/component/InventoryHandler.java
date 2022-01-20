package com.github.almostreliable.lazierae2.component;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryHandler extends ItemStackHandler {

    private final TileEntity tile;

    public InventoryHandler(TileEntity tile, int size) {
        super(size);
        this.tile = tile;
    }

    @Override
    protected void onContentsChanged(int slot) {
        tile.setChanged();
    }
}
