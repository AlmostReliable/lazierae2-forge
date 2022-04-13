package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ControllerBlock extends Block {
    public static int MIN_BLOCK = 5;
    public static int MAX_SIZE = 13;
    public static BooleanProperty VALID = BooleanProperty.create("valid");
    public static DirectionProperty FACING = DirectionProperty.create("facing",
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST
    );

    public ControllerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(VALID, FACING);
    }
}
