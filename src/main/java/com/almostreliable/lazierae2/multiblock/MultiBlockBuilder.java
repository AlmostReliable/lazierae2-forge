package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiBlockBuilder {

    private final int maxSize;
    private final int minSize;
    private Predicate<BlockState> validOutsideBlockPredicate = blockState -> true;

    public MultiBlockBuilder(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Nullable
    public MultiBlock build(Level level, BlockPos clickedBlockPos, Direction facing) {
        Direction[] iterationDirection = getIterationDirection(facing);
        Direction mDir = iterationDirection[0];
        Direction rDir = iterationDirection[1];
        Direction cDir = iterationDirection[2];

        LinkedList<BlockPos> horizontalRow = findValidRow(level, clickedBlockPos, rDir.getOpposite(), rDir);
        LinkedList<BlockPos> verticalRow = findValidRow(level, clickedBlockPos, cDir.getOpposite(), cDir);

        BlockPos startPosition = getStartPosition(clickedBlockPos, horizontalRow, verticalRow);

        return null;
    }

    private BlockPos getStartPosition(
        BlockPos blockPos, LinkedList<BlockPos> horizontalRow, LinkedList<BlockPos> verticalRow
    ) {
        BlockPos horizontalToController = horizontalRow.getFirst().subtract(blockPos);
        BlockPos verticalToController = verticalRow.getFirst().subtract(blockPos);
        return blockPos.offset(horizontalToController.offset(verticalToController));
    }

    protected Direction[] getIterationDirection(Direction direction) {
        return switch (direction) {
            case UP -> new Direction[]{Direction.DOWN, Direction.EAST, Direction.SOUTH};
            case DOWN -> new Direction[]{Direction.UP, Direction.WEST, Direction.NORTH};
            default -> new Direction[]{direction.getOpposite(), direction.getOpposite().getClockWise(), Direction.UP};
        };
    }

    protected LinkedList<BlockPos> findValidRow(
        Level level, BlockPos startPos, @Nullable Direction negativeDirection, @Nullable Direction positiveDirection
    ) {
        LinkedList<BlockPos> frontRow = new LinkedList<>();
        frontRow.add(startPos.immutable());

        BlockPos toPositive = positiveDirection == null ? null : startPos.relative(positiveDirection);
        BlockPos toNegative = negativeDirection == null ? null : startPos.relative(negativeDirection);

        boolean changed;
        do {
            changed = false;

            if (toPositive != null) {
                BlockState positiveBlocKState = level.getBlockState(toPositive);
                if (validOutsideBlockPredicate.test(positiveBlocKState)) {
                    frontRow.addLast(toPositive);
                    toPositive = toPositive.relative(positiveDirection);
                    changed = true;
                }
            }

            if (toNegative != null) {
                BlockState negativeBlockState = level.getBlockState(toNegative);
                if (validOutsideBlockPredicate.test(negativeBlockState)) {
                    frontRow.addFirst(toNegative);
                    toNegative = toNegative.relative(negativeDirection);
                    changed = true;
                }
            }
        } while (frontRow.size() <= maxSize && changed);

        return frontRow;
    }
    @Nullable
    protected BlockPos findNextValidBlockPos(Level level, BlockPos curPos, Consumer<BlockPos> consumer) {
        // BlockState blockState = level.getBlockState(curPos);
        // if (validOutsideBlockPredicate.test(blockState)) {
        //     consumer.accept(curPos);
        //     return curPos.relative(positiveDirection);
        // }

        return null;
    }
}
