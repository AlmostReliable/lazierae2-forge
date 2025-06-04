package com.almostreliable.lazierae.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.blockentity.CrystalInjectorBlockEntity;

public class CrystalInjectorBlock extends MachineBlock<CrystalInjectorBlockEntity> {

    public CrystalInjectorBlock(Properties props) {
        super(props);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving
    ) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (level.isClientSide || !pos.above().equals(fromPos)) return;

        // TODO: check in which order this fires if the recipe replaces the block with the result
        if (level.getBlockEntity(pos) instanceof CrystalInjectorBlockEntity injector) {
            injector.onNeighborChange();
        }
    }
}
