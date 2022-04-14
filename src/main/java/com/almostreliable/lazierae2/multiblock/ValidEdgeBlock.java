package com.almostreliable.lazierae2.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ValidEdgeBlock extends AbstractValidBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ValidEdgeBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(POWERED);
    }
}
