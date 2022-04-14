package com.almostreliable.lazierae2.multiblock;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
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

        boolean valid = new MultiBlockValidator(MIN_SIZE, MAX_SIZE)
            .onValidateCenter((l, s, p) -> s.getBlock() == Blocks.AIR)
            .onValidateEdge((l, s, p) -> s.getBlock() == Blocks.DIAMOND_BLOCK)
            .onValidateWall((l, s, p) -> s.getBlock() == Blocks.GOLD_BLOCK || p.equals(blockPos))
            .onValidateCorner((l, s, p) -> s.getBlock() == Blocks.DIAMOND_BLOCK)
            .validate(level, blockPos, blockState.getValue(FACING));

        if (!valid) {
            player.sendMessage(new TextComponent("Invalid multiblock"), player.getUUID());
            return InteractionResult.FAIL;
        }

        player.sendMessage(new TextComponent("Valid multiblock"), player.getUUID());
        return InteractionResult.CONSUME;
    }

    public Direction[] getIterationDirection(Direction direction) {
        return switch (direction) {
            case UP -> new Direction[]{Direction.DOWN, Direction.EAST, Direction.SOUTH};
            case DOWN -> new Direction[]{Direction.UP, Direction.WEST, Direction.NORTH};
            default -> new Direction[]{direction.getOpposite(), direction.getOpposite().getClockWise(), Direction.UP};
        };
    }

    public LinkedList<BlockPos> findValidRow(
        Level level, BlockPos blockPos, Direction negativeDirection, Direction positiveDirection
    ) {
        LinkedList<BlockPos> frontRow = new LinkedList<>();
        frontRow.add(blockPos.immutable());

        boolean continueNegative = true;
        boolean continuePositive = true;
        do {

            if (continuePositive &&
                !blockPosIsValid(level, frontRow.getLast().relative(positiveDirection), frontRow::addLast)) {
                continuePositive = false;
            }

            if (continueNegative &&
                !blockPosIsValid(level, frontRow.getFirst().relative(negativeDirection), frontRow::addFirst)) {
                continueNegative = false;
            }
        } while (frontRow.size() <= MAX_SIZE && (continueNegative || continuePositive));

        return frontRow;
    }

    protected boolean blockPosIsValid(Level level, BlockPos curPos, Consumer<BlockPos> consumer) {
        BlockState blockState = level.getBlockState(curPos);
        if (blockState.getBlock() instanceof ValidEdgeBlock) {
            consumer.accept(curPos);
            return true;
        }
        return false;
    }

    public Collection<BlockPos> findValidBlockPos(Level level, BlockPos blockPos, Direction direction, int maxSteps) {
        Set<BlockPos> blockPosSet = new HashSet<>();
        BlockPos curPos;
        for (int i = 0; i < maxSteps; i++) {
            curPos = blockPos.relative(direction);
            BlockState curBlockState = level.getBlockState(curPos);

            if (!(curBlockState.getBlock() instanceof ValidEdgeBlock)) {
                return blockPosSet;
            }

            blockPosSet.add(curPos);
        }
        return blockPosSet;
    }
}
