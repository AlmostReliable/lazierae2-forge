package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AssemblerBlock extends GenericBlock {

    public abstract boolean isValidMultiBlockPos(PositionType posType);

    public boolean isMultiBlock(BlockState state) {
        return state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE);
    }

    public BlockState setupMultiBlockState(BlockState state, BlockPos hullPos, BlockPos controllerPos) {
        return state
            .setValue(GenericBlock.ACTIVE, true);
    }
}
