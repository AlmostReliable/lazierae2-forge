package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class AbstractAssemblerBlock extends Block {
    public static final OptionalDirectionProperty CTRL_HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty CTRL_VERTICAL = OptionalDirectionProperty.VERTICAL;
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    public AbstractAssemblerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(CTRL_HORIZONTAL, OptionalDirection.NONE)
            .setValue(CTRL_VERTICAL, OptionalDirection.NONE)
            .setValue(VALID, false));
    }

    @Override
    public InteractionResult use(
        BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(blockState, level, blockPos, player, hand, hit);
        }

        OptionalDirection horizontalDirection = blockState.getValue(CTRL_HORIZONTAL);
        OptionalDirection verticalDirection = blockState.getValue(CTRL_VERTICAL);

        Tuple<BlockState, BlockPos> found = findControllerBlockState(level,
            blockPos,
            horizontalDirection,
            verticalDirection
        );
        // TODO open guy

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (newState.isAir()) {
            Tuple<BlockState, BlockPos> controllerTuple = findControllerBlockState(level,
                blockPos,
                oldState.getValue(CTRL_HORIZONTAL),
                oldState.getValue(CTRL_VERTICAL)
            );
            if (controllerTuple != null && controllerTuple.getA().getBlock() instanceof ControllerBlock cb) {
                cb.invalidate(level, controllerTuple.getA(), controllerTuple.getB());
            }
        }
    }

    @Nullable
    public Tuple<BlockState, BlockPos> findControllerBlockState(
        Level level, BlockPos blockPos, OptionalDirection horizontalDirection, OptionalDirection verticalDirection
    ) {
        if (horizontalDirection == OptionalDirection.NONE && verticalDirection == OptionalDirection.NONE) {
            return null;
        }

        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for (int i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            horizontalDirection.relative(mutable);
            verticalDirection.relative(mutable);

            BlockState relativeBlockState = level.getBlockState(mutable);
            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return new Tuple<>(relativeBlockState, mutable.immutable());
            }

            if (relativeBlockState.getBlock() instanceof AbstractAssemblerBlock) {
                OptionalDirection horizontal = relativeBlockState.getValue(CTRL_HORIZONTAL);
                OptionalDirection vertical = relativeBlockState.getValue(CTRL_VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    public BlockState createValidBlockState(BlockPos blockPos, BlockPos lookPos) {
        OptionalDirection horizontalOffset = getHorizontalOffset(blockPos, lookPos);
        OptionalDirection verticalOffset = getVerticalOffset(blockPos, lookPos);

        return defaultBlockState()
            .setValue(AbstractAssemblerBlock.CTRL_HORIZONTAL, horizontalOffset)
            .setValue(AbstractAssemblerBlock.CTRL_VERTICAL, verticalOffset)
            .setValue(AbstractAssemblerBlock.VALID, true);
    }

    public boolean isValid(BlockState blockState) {
        return blockState.getBlock() instanceof AbstractAssemblerBlock && blockState.getValue(VALID);
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CTRL_HORIZONTAL, CTRL_VERTICAL, VALID);
    }
}
