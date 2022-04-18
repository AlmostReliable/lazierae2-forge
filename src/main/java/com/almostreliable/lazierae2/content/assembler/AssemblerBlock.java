package com.almostreliable.lazierae2.content.assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class AssemblerBlock extends Block {

    public static final OptionalDirectionProperty CTRL_HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty CTRL_VERTICAL = OptionalDirectionProperty.VERTICAL;
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    protected AssemblerBlock(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(CTRL_HORIZONTAL, OptionalDirection.NONE)
            .setValue(CTRL_VERTICAL, OptionalDirection.NONE)
            .setValue(VALID, false);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(CTRL_HORIZONTAL, CTRL_VERTICAL, VALID);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.isAir()) {
            var controllerTuple = findControllerBlockState(level,
                pos,
                oldState.getValue(CTRL_HORIZONTAL),
                oldState.getValue(CTRL_VERTICAL)
            );
            if (controllerTuple != null && controllerTuple.getA().getBlock() instanceof ControllerBlock cb) {
                cb.invalidate(level, controllerTuple.getA(), controllerTuple.getB());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(state, level, pos, player, hand, hit);
        }

        var horizontalDirection = state.getValue(CTRL_HORIZONTAL);
        var verticalDirection = state.getValue(CTRL_VERTICAL);

        var found = findControllerBlockState(level, pos, horizontalDirection, verticalDirection);
        // TODO open gui

        return InteractionResult.CONSUME;
    }

    @Nullable
    public Tuple<BlockState, BlockPos> findControllerBlockState(
        Level level, BlockPos pos, OptionalDirection horizontalDirection, OptionalDirection verticalDirection
    ) {
        if (horizontalDirection == OptionalDirection.NONE && verticalDirection == OptionalDirection.NONE) {
            return null;
        }

        var mutable = pos.mutable();
        for (var i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            horizontalDirection.relative(mutable);
            verticalDirection.relative(mutable);

            var relativeBlockState = level.getBlockState(mutable);
            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return new Tuple<>(relativeBlockState, mutable.immutable());
            }

            if (relativeBlockState.getBlock() instanceof AssemblerBlock) {
                var horizontal = relativeBlockState.getValue(CTRL_HORIZONTAL);
                var vertical = relativeBlockState.getValue(CTRL_VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    public BlockState createValidBlockState(BlockPos blockPos, BlockPos lookPos) {
        var horizontalOffset = getHorizontalOffset(blockPos, lookPos);
        var verticalOffset = getVerticalOffset(blockPos, lookPos);

        return defaultBlockState()
            .setValue(CTRL_HORIZONTAL, horizontalOffset)
            .setValue(CTRL_VERTICAL, verticalOffset)
            .setValue(VALID, true);
    }

    public boolean isValid(BlockState state) {
        return state.getBlock() instanceof AssemblerBlock && state.getValue(VALID);
    }

    protected OptionalDirection getVerticalOffset(BlockPos blockPos, BlockPos lookPos) {
        if (lookPos.getY() == blockPos.getY()) {
            return OptionalDirection.NONE;
        }

        return lookPos.getY() < blockPos.getY() ? OptionalDirection.DOWN : OptionalDirection.UP;
    }

    protected OptionalDirection getHorizontalOffset(BlockPos blockPos, BlockPos lookPos) {
        if (lookPos.getX() == blockPos.getX() && lookPos.getZ() == blockPos.getZ()) {
            return OptionalDirection.NONE;
        }

        if (lookPos.getZ() > blockPos.getZ()) {
            return OptionalDirection.SOUTH;
        }

        if (lookPos.getZ() < blockPos.getZ()) {
            return OptionalDirection.NORTH;
        }

        if (lookPos.getX() > blockPos.getX()) {
            return OptionalDirection.EAST;
        }

        return OptionalDirection.WEST;
    }
}
