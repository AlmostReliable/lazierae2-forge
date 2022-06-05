package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericBlock;
import net.minecraft.world.level.block.state.BlockState;

abstract class AssemblerBlock extends GenericBlock {
    boolean isMultiBlock(BlockState state) {
        return state.getValue(GenericBlock.ACTIVE).equals(Boolean.TRUE);
    }
}
