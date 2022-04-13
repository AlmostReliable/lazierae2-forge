package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.function.IntPredicate;

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

        System.out.println("==== Use ====");
        // Direction direction = blockState.getValue(FACING);
        // Direction left = direction.getClockWise();
        // Direction right = direction.getCounterClockWise();

        Direction direction = blockState.getValue(FACING);
        Direction[] iterationDirection = getIterationDirection(direction);
        Direction mDir = iterationDirection[0];
        Direction rDir = iterationDirection[1];
        Direction cDir = iterationDirection[2];

        LinkedList<BlockPos> horizontalRow = findValidRow(level, blockPos, rDir.getOpposite(), rDir);
        LinkedList<BlockPos> verticalRow = findValidRow(level, blockPos, cDir.getOpposite(), cDir);

        System.out.println(horizontalRow);
        System.out.println(verticalRow);

        BlockPos horizontalToController = horizontalRow.getFirst().subtract(blockPos);
        BlockPos verticalToController = verticalRow.getFirst().subtract(blockPos);
        BlockPos startPos = blockPos.offset(horizontalToController.offset(verticalToController));
        System.out.println("H to C:   " + horizontalToController.toShortString());
        System.out.println("V to C:   " + verticalToController.toShortString());
        System.out.println("StartPos: " + startPos.toShortString());

        int size = horizontalRow.size();

        if (size != verticalRow.size() || size < MIN_SIZE) {
            return InteractionResult.FAIL;
        }

        Block wallBlock = Blocks.STONE; //Blocks.GREEN_STAINED_GLASS;
        Block edgeBlock = Blocks.STONE; //Blocks.RED_STAINED_GLASS;
        Block centerBlock = Blocks.STONE; //Blocks.BLUE_STAINED_GLASS;
        Block borderBlock = Blocks.STONE; //Blocks.MAGENTA_STAINED_GLASS;

        IntPredicate isCenter = value -> 0 < value && value < size - 1;
        IntPredicate isEdge = value -> value == 0 || value == size - 1;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    BlockPos position = startPos.relative(mDir, i).relative(rDir, j).relative(cDir, k);
                    BlockState curState = level.getBlockState(position);
                    if (!(curState.getBlock() instanceof WallBlock || curState.getBlock() instanceof ControllerBlock)) {
                        if (position.equals(startPos)) {
                            level.setBlock(position, Blocks.STONE.defaultBlockState(), 2 | 16);
                        } else if (isCenter.test(i) && isCenter.test(j) && isCenter.test(k)) {
                            level.setBlock(position, centerBlock.defaultBlockState(), 2 | 16);
                        } else if (isEdge.test(i) && isEdge.test(j) && isEdge.test(k)) {
                            level.setBlock(position, edgeBlock.defaultBlockState(), 2 | 16);
                        } else if (isEdge.test(i) && isEdge.test(j) || isEdge.test(i) && isEdge.test(k) ||
                            isEdge.test(j) && isEdge.test(k)) {
                            level.setBlock(position, borderBlock.defaultBlockState(), 2 | 16);
                        } else {
                            level.setBlock(position, wallBlock.defaultBlockState(), 2 | 16);
                        }
                    }
                }
            }
        }
        // for (Integer d : depth) {
        // for (Integer r : row) {
        //     for (Integer c : column) {
        //         BlockPos cur = new BlockPos(startPos.getX(), startPos.getY() + c, startPos.getZ() + r);
        //         System.out.println(cur.toShortString());
        //
        //         BlockState curState = level.getBlockState(cur);
        //         if (!(curState.getBlock() instanceof WallBlock)) {
        //             Block toSet = curState.getBlock() == Blocks.STONE ? Blocks.DIORITE : Blocks.STONE; // TODO Debug
        //             level.setBlock(cur, toSet.defaultBlockState(), 2 | 16);
        //         }
        //     }
        // }
        // }
        // BlockPos topRight = horizontalRow.getLast().above(verticalRow.getLast().getY() - blockPos.getY());
        // BlockPos bottomLeft = horizontalRow.getFirst().below(verticalRow.getFirst().getY() - blockPos.getY());
        // BlockPos bottomRight = horizontalRow.getLast().below(verticalRow.getFirst().getY() - blockPos.getY());

        // BlockPos.MutableBlockPos cur = bottomLeft.mutable();
        // for (int depth = 0; depth < size; depth++) {
        //     for (int column = 0; column < size; column++) {
        //         for (int row = 0; row < size; row++) {
        //
        //         }
        //     }
        //     cur = cur.relative(mDir).mutable();
        // }

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

            if (continuePositive && !blockPosIsValid(level, frontRow.getLast().relative(positiveDirection), frontRow::addLast)) {
                continuePositive = false;
            }

            if (continueNegative && !blockPosIsValid(level, frontRow.getFirst().relative(negativeDirection), frontRow::addFirst)) {
                continueNegative = false;
            }
        } while (frontRow.size() <= MAX_SIZE && (continueNegative || continuePositive));

        return frontRow;
    }

    protected boolean blockPosIsValid(Level level, BlockPos curPos, Consumer<BlockPos> consumer) {
        BlockState blockState = level.getBlockState(curPos);
        if (blockState.getBlock() instanceof WallBlock) {
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

            if (!(curBlockState.getBlock() instanceof WallBlock)) {
                return blockPosSet;
            }

            blockPosSet.add(curPos);
        }
        return blockPosSet;
    }
}
