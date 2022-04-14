package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.util.Predicate3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;

public class MultiBlockValidator {
    protected static final int INCLUDED_CONTROLLER_POS = 1;
    protected static final Predicate3<Level, BlockState, BlockPos> ALWAYS_TRUE = (level, blockState, blockPos) -> true;
    protected final int maxSize;
    protected final int minSize;
    protected Predicate3<Level, BlockState, BlockPos> centerHandler = ALWAYS_TRUE;
    protected Predicate3<Level, BlockState, BlockPos> wallHandler = ALWAYS_TRUE;
    protected Predicate3<Level, BlockState, BlockPos> cornerHandler = ALWAYS_TRUE;
    protected Predicate3<Level, BlockState, BlockPos> edgeHandler = ALWAYS_TRUE;

    public MultiBlockValidator(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public boolean validate(Level level, BlockPos controllerBlockPos, Direction facing) {
        Direction[] iterationDirection = getIterationDirections(facing);
        Direction mDir = iterationDirection[0];
        Direction rDir = iterationDirection[1];
        Direction cDir = iterationDirection[2];

        SizeCheckResult negativeRowResult = findWall(level, controllerBlockPos, rDir.getOpposite());
        SizeCheckResult positiveRowResult = findWall(level, controllerBlockPos, rDir);
        SizeCheckResult negativeColumnResult = findWall(level, controllerBlockPos, cDir.getOpposite());
        SizeCheckResult positiveColumnResult = findWall(level, controllerBlockPos, cDir);

        if (negativeRowResult == null || positiveRowResult == null || negativeColumnResult == null ||
            positiveColumnResult == null) {
            return false;
        }

        int size = negativeRowResult.size() + INCLUDED_CONTROLLER_POS + positiveRowResult.size();
        if (size != negativeColumnResult.size() + INCLUDED_CONTROLLER_POS + positiveColumnResult.size() ||
            size < minSize) {
            return false;
        }

        BlockPos startPosition = getStartPosition(controllerBlockPos,
            negativeRowResult.blockPos(),
            negativeColumnResult.blockPos()
        );

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    int x = startPosition.getX() + (mDir.getStepX() * i) + (rDir.getStepX() * j) + (cDir.getStepX() * k);
                    int y = startPosition.getY() + (mDir.getStepY() * i) + (rDir.getStepY() * j) + (cDir.getStepY() * k);
                    int z = startPosition.getZ() + (mDir.getStepZ() * i) + (rDir.getStepZ() * j) + (cDir.getStepZ() * k);
                    BlockPos position = new BlockPos(x, y, z);
                    if (!handleEachPosition(level, position, i, j, k, size)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public MultiBlockValidator onValidateWall(Predicate3<Level, BlockState, BlockPos> wallHandler) {
        Objects.requireNonNull(wallHandler);
        this.wallHandler = wallHandler;
        return this;
    }

    public MultiBlockValidator onValidateCenter(Predicate3<Level, BlockState, BlockPos> centerHandler) {
        Objects.requireNonNull(centerHandler);
        this.centerHandler = centerHandler;
        return this;
    }

    public MultiBlockValidator onValidateCorner(Predicate3<Level, BlockState, BlockPos> cornerHandler) {
        Objects.requireNonNull(cornerHandler);
        this.cornerHandler = cornerHandler;
        return this;
    }

    public MultiBlockValidator onValidateEdge(Predicate3<Level, BlockState, BlockPos> edgeHandler) {
        Objects.requireNonNull(edgeHandler);
        this.edgeHandler = edgeHandler;
        return this;
    }

    protected boolean handleEachPosition(Level level, BlockPos blockPos, int i, int j, int k, int size) {
        BlockState blockState = level.getBlockState(blockPos);
        if (isCenter(i, j, k, size)) {
            return centerHandler.test(level, blockState, blockPos);
        } else if (isCorner(i, j, k, size)) {
            return cornerHandler.test(level, blockState, blockPos);
        } else if (isEdge(i, j, k, size)) {
            return edgeHandler.test(level, blockState, blockPos);
        } else {
            return wallHandler.test(level, blockState, blockPos);
        }
    }

    protected boolean isCenter(int i, int j, int k, int size) {
        return (0 < i && i < size - 1) && (0 < j && j < size - 1) && (0 < k && k < size - 1);
    }

    protected boolean isCorner(int i, int j, int k, int size) {
        return (i == 0 || i == size - 1) && (j == 0 || j == size - 1) && (k == 0 || k == size - 1);
    }

    protected boolean isEdge(int i, int j, int k, int size) {
        boolean iTest = i == 0 || i == size - 1;
        boolean jTest = j == 0 || j == size - 1;
        boolean kTest = k == 0 || k == size - 1;
        return iTest && jTest || iTest && kTest || jTest && kTest;
    }

    protected BlockPos getStartPosition(BlockPos blockPos, BlockPos negativeRowPos, BlockPos positiveRowPos) {
        BlockPos hPos = negativeRowPos.subtract(blockPos);
        BlockPos vPos = positiveRowPos.subtract(blockPos);
        return blockPos.offset(hPos.offset(vPos));
    }

    protected Direction[] getIterationDirections(Direction direction) {
        return switch (direction) {
            case UP -> new Direction[]{Direction.DOWN, Direction.EAST, Direction.SOUTH};
            case DOWN -> new Direction[]{Direction.UP, Direction.WEST, Direction.NORTH};
            default -> new Direction[]{direction.getOpposite(), direction.getOpposite().getClockWise(), Direction.UP};
        };
    }

    @Nullable
    protected SizeCheckResult findWall(Level level, BlockPos fromPos, Direction direction) {
        for (int i = 1; i < maxSize; i++) {
            BlockPos curPos = fromPos.relative(direction, i);
            BlockState blockState = level.getBlockState(curPos);
            if (edgeHandler.test(level, blockState, curPos)) {
                return new SizeCheckResult(curPos, i);
            }
        }

        return null;
    }

    public record SizeCheckResult(BlockPos blockPos, int size) {}
}
