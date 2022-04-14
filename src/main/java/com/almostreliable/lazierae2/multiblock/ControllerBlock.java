package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.core.Setup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ControllerBlock extends Block {
    public static int MIN_SIZE = 5;
    public static int MAX_SIZE = 13;
    public static BooleanProperty VALID = BooleanProperty.create("valid");
    public static DirectionProperty FACING = DirectionalBlock.FACING;

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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(VALID, false);
    }

    @Override
    public InteractionResult use(
        BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(blockState, level, blockPos, player, hand, hit);
        }

        Set<BlockPos> walls = new HashSet<>();
        Set<BlockPos> edges = new HashSet<>();

        boolean valid = new MultiBlockValidator(MIN_SIZE, MAX_SIZE)
            .onValidateCenter((l, s, p) -> s.getBlock() == Blocks.AIR)
            .onValidateEdge((l, s, p) -> consumeEdgeIfValid(s, p, edges::add))
            .onValidateWall((l, s, p) -> consumeWallIfValid(s, p, walls::add))
            .onValidateCorner((l, s, p) -> consumeEdgeIfValid(s, p, edges::add))
            .validate(level, blockPos, blockState.getValue(FACING));

        if (!valid) {
            player.sendMessage(new TextComponent("Invalid multiblock"), player.getUUID());
            return InteractionResult.FAIL;
        }

        for (BlockPos wallPos : walls) {
            OptionalDirection horizontalOffset = getHorizontalOffset(blockPos, wallPos);
            OptionalDirection verticalOffset = getVerticalOffset(blockPos, wallPos);
            level.setBlock(wallPos,
                Setup.Blocks.VALID_WALL_BLOCK
                    .get()
                    .defaultBlockState()
                    .setValue(AbstractValidBlock.CTRL_HORIZONTAL, horizontalOffset)
                    .setValue(AbstractValidBlock.CTRL_VERTICAL, verticalOffset),
                2 | 16,
                1
            );
        }

        for (BlockPos edgePos : edges) {
            OptionalDirection horizontalOffset = getHorizontalOffset(blockPos, edgePos);
            OptionalDirection verticalOffset = getVerticalOffset(blockPos, edgePos);
            level.setBlock(
                edgePos,
                Setup.Blocks.VALID_EDGE_BLOCK
                    .get()
                    .defaultBlockState()
                    .setValue(AbstractValidBlock.CTRL_HORIZONTAL, horizontalOffset)
                    .setValue(AbstractValidBlock.CTRL_VERTICAL, verticalOffset),
                2 | 16,
                1
            );
        }

        level.setBlock(blockPos, blockState.setValue(VALID, true), 3);
        player.sendMessage(new TextComponent("Valid multiblock"), player.getUUID());
        return InteractionResult.CONSUME;
    }

    public boolean consumeEdgeIfValid(BlockState blockState, BlockPos blockPos, Consumer<BlockPos> consumer) {
        if (blockState.getBlock() == Blocks.DIAMOND_BLOCK) {
            consumer.accept(blockPos);
            return true;
        }
        return false;
    }

    public boolean consumeWallIfValid(BlockState blockState, BlockPos blockPos, Consumer<BlockPos> consumer) {
        if (blockState.getBlock() == Blocks.GOLD_BLOCK) {
            consumer.accept(blockPos);
            return true;
        }
        return false;
    }

    public OptionalDirection getVerticalOffset(BlockPos centerPos, BlockPos blockPos) {
        if (centerPos.getY() == blockPos.getY()) {
            return OptionalDirection.NONE;
        }

        return centerPos.getY() < blockPos.getY() ? OptionalDirection.DOWN : OptionalDirection.UP;
    }

    public OptionalDirection getHorizontalOffset(BlockPos centerPos, BlockPos blockPos) {
        if (centerPos.getX() == blockPos.getX() && centerPos.getZ() == blockPos.getZ()) {
            return OptionalDirection.NONE;
        }

        if (centerPos.getZ() > blockPos.getZ()) {
            return OptionalDirection.SOUTH;
        }

        if (centerPos.getZ() < blockPos.getZ()) {
            return OptionalDirection.NORTH;
        }

        if (centerPos.getX() > blockPos.getX()) {
            return OptionalDirection.EAST;
        }

        return OptionalDirection.WEST;
    }
}
