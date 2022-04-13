package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import javax.annotation.Nullable;

public class ValidWallBlock extends Block {
    public static final DirectionProperty CONTROLLER_DIRECTION = DirectionProperty.create("controller_direction",
        Direction.NORTH,
        Direction.EAST,
        Direction.SOUTH,
        Direction.WEST,
        Direction.UP,
        Direction.DOWN
    );

    public ValidWallBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any().setValue(CONTROLLER_DIRECTION, Direction.NORTH));
    }

    @Nullable
    protected BlockState findControllerBlockState(
        Level level, BlockPos blockPos, BlockState blockState, Direction direction
    ) {
        for (int i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            BlockPos relative = blockPos.relative(direction);
            BlockState relativeBlockState = level.getBlockState(relative);

            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return relativeBlockState;
            }

            if (relativeBlockState.getBlock() instanceof ValidWallBlock) {
                return findControllerBlockState(level,
                    relative,
                    relativeBlockState,
                    relativeBlockState.getValue(CONTROLLER_DIRECTION)
                );
            }
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CONTROLLER_DIRECTION);
    }
}
