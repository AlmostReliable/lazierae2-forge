package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.core.TypeEnums.HULL_TYPE;
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
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class HullBlock extends AssemblerBlock {

    public static final OptionalDirectionProperty HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty VERTICAL = OptionalDirectionProperty.VERTICAL;
    public final HULL_TYPE type;

    public HullBlock(HULL_TYPE type) {
        this.type = type;
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(HORIZONTAL, OptionalDirection.NONE)
            .setValue(VERTICAL, OptionalDirection.NONE)
            .setValue(IS_MULTIBLOCK, false)
            .setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(HORIZONTAL, OptionalDirection.NONE).setValue(VERTICAL, OptionalDirection.NONE);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL, VERTICAL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.isAir()) {
            var controllerTuple = findControllerBlockState(level,
                pos,
                oldState.getValue(HORIZONTAL),
                oldState.getValue(VERTICAL)
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

        var horizontalDirection = state.getValue(HORIZONTAL);
        var verticalDirection = state.getValue(VERTICAL);

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

            if (relativeBlockState.getBlock() instanceof HullBlock) {
                var horizontal = relativeBlockState.getValue(HORIZONTAL);
                var vertical = relativeBlockState.getValue(VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    public BlockState createDefaultMultiBlockState(BlockPos blockPos, BlockPos lookPos) {
        var horizontalOffset = getHorizontalOffset(blockPos, lookPos);
        var verticalOffset = getVerticalOffset(blockPos, lookPos);

        return defaultBlockState()
            .setValue(HORIZONTAL, horizontalOffset)
            .setValue(VERTICAL, verticalOffset)
            .setValue(IS_MULTIBLOCK, true)
            .setValue(ACTIVE, true); // TODO probably just set this active if energy exist?
    }

    public boolean isUsableForMultiBlock(BlockState state) {
        return state.getBlock() instanceof HullBlock && !state.getValue(IS_MULTIBLOCK);
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
