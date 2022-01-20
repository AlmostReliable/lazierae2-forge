package com.github.almostreliable.lazierae2.block;

import com.github.almostreliable.lazierae2.tile.AggregatorTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class AggregatorBlock extends MachineBlock {

    @Nullable
    @Override
    public TileEntity createTileEntity(
        BlockState state, IBlockReader world
    ) {
        return new AggregatorTile();
    }
}
