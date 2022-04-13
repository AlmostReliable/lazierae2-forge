package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

public class ValidWallBlock extends Block {
    public static final OptionalDirectionProperty CTRL_HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty CTRL_VERTICAL = OptionalDirectionProperty.VERTICAL;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ValidWallBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(CTRL_HORIZONTAL, OptionalDirection.NONE)
            .setValue(CTRL_VERTICAL, OptionalDirection.NONE)
            .setValue(POWERED, false));
    }

    @Nullable
    public BlockState findControllerBlockState(
        Level level, BlockPos blockPos, OptionalDirection horizontalDirection, OptionalDirection verticalDirection
    ) {
        for (int i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            BlockPos.MutableBlockPos mutable = blockPos.mutable();
            horizontalDirection.relative(mutable);
            verticalDirection.relative(mutable);

            BlockState relativeBlockState = level.getBlockState(mutable);
            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return relativeBlockState;
            }

            if (relativeBlockState.getBlock() instanceof ValidWallBlock) {
                OptionalDirection horizontal = relativeBlockState.getValue(CTRL_HORIZONTAL);
                OptionalDirection vertical = relativeBlockState.getValue(CTRL_VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CTRL_HORIZONTAL, CTRL_VERTICAL, POWERED);
    }
}
