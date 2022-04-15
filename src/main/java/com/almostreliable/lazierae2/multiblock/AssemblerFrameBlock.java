package com.almostreliable.lazierae2.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AssemblerFrameBlock extends AbstractAssemblerBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public AssemblerFrameBlock(Properties props) {
        super(props);
        this.registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(POWERED);
    }
}
