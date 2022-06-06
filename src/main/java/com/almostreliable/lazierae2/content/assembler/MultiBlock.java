package com.almostreliable.lazierae2.content.assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

final class MultiBlock {

    static final int MAX_SIZE = 13;
    private static final int MIN_SIZE = 5;

    private MultiBlock() {}

    static boolean iterateMultiBlock(
        MultiBlockData data, IterateCallback callback
    ) {
        for (var x = 0; x < data.size(); x++) {
            for (var y = 0; y < data.size(); y++) {
                for (var z = 0; z < data.size(); z++) {
                    var position = data.itDirs().relative(data.startPosition(), x, y, z);
                    var type = Type.of(x, y, z, data.size());
                    if (!callback.apply(type, position)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public enum Type {
        WALL, CORNER, EDGE, INNER;

        static Type of(int x, int y, int z, int size) {
            if (isInner(x, y, z, size)) {
                return INNER;
            }
            if (isCorner(x, y, z, size)) {
                return CORNER;
            }
            if (isEdge(x, y, z, size)) {
                return EDGE;
            }
            return WALL;
        }

        private static boolean isInner(int x, int y, int z, int size) {
            return x > 0 && x < size - 1 && y > 0 && y < size - 1 && z > 0 && z < size - 1;
        }

        private static boolean isCorner(int x, int y, int z, int size) {
            return (x == 0 || x == size - 1) && (y == 0 || y == size - 1) && (z == 0 || z == size - 1);
        }

        private static boolean isEdge(int x, int y, int z, int size) {
            var iTest = x == 0 || x == size - 1;
            var jTest = y == 0 || y == size - 1;
            var kTest = z == 0 || z == size - 1;
            return iTest && jTest || iTest && kTest || jTest && kTest;
        }
    }

    @FunctionalInterface
    public interface IterateCallback {
        boolean apply(Type type, BlockPos currentPos);
    }

    record IterateDirections(Direction depthDirection, Direction rowDirection, Direction columnDirection) {

        static IterateDirections of(Direction facing) {
            if (facing == Direction.UP) {
                return new IterateDirections(Direction.DOWN, Direction.EAST, Direction.SOUTH);
            }
            if (facing == Direction.DOWN) {
                return new IterateDirections(Direction.UP, Direction.WEST, Direction.NORTH);
            }
            return new IterateDirections(facing.getOpposite(), facing.getOpposite().getClockWise(), Direction.UP);
        }

        private BlockPos relative(BlockPos blockPos, int x, int y, int z) {
            var relativeX = calculatePosition(
                blockPos.getX(),
                depthDirection.getStepX() * x,
                rowDirection.getStepX() * y,
                columnDirection.getStepX() * z
            );
            var relativeY = calculatePosition(
                blockPos.getY(),
                depthDirection.getStepY() * x,
                rowDirection.getStepY() * y,
                columnDirection.getStepY() * z
            );
            var relativeZ = calculatePosition(
                blockPos.getZ(),
                depthDirection.getStepZ() * x,
                rowDirection.getStepZ() * y,
                columnDirection.getStepZ() * z
            );
            return new BlockPos(relativeX, relativeY, relativeZ);
        }

        private int calculatePosition(int initial, int depth, int row, int column) {
            return initial + depth + row + column;
        }
    }

    public record MultiBlockData(int size, BlockPos startPosition, IterateDirections itDirs) {

        @Nullable
        public static MultiBlockData of(
            BlockPos originPos, IterateDirections itDirs, Predicate<? super BlockPos> edgeCheck
        ) {
            var negativeRowResult = findEdge(originPos, itDirs.rowDirection().getOpposite(), edgeCheck);
            var positiveRowResult = findEdge(originPos, itDirs.rowDirection(), edgeCheck);
            var negativeColumnResult = findEdge(originPos, itDirs.columnDirection().getOpposite(), edgeCheck);
            var positiveColumnResult = findEdge(originPos, itDirs.columnDirection(), edgeCheck);
            if (negativeRowResult == null || positiveRowResult == null || negativeColumnResult == null ||
                positiveColumnResult == null) {
                return null;
            }

            var sizeRow = negativeRowResult.controllerOffset() + positiveRowResult.controllerOffset() + 1;
            var sizeColumn = negativeColumnResult.controllerOffset() + positiveColumnResult.controllerOffset() + 1;
            if (sizeRow != sizeColumn || sizeRow < MIN_SIZE) {
                return null;
            }

            var startPosition = getStartPosition(
                originPos,
                negativeRowResult.blockPos(),
                negativeColumnResult.blockPos()
            );
            return new MultiBlockData(sizeRow, startPosition, itDirs);
        }

        static MultiBlockData load(CompoundTag tag) {
            var size = tag.getInt(SIZE_ID);
            var startPosition = NbtUtils.readBlockPos(tag.getCompound(START_POS_ID));
            var mDir = Direction.values()[tag.getInt(DEPTH_DIR_ID)];
            var rDir = Direction.values()[tag.getInt(ROW_DIR_ID)];
            var cDir = Direction.values()[tag.getInt(COL_DIR_ID)];
            return new MultiBlockData(size, startPosition, new IterateDirections(mDir, rDir, cDir));
        }

        static CompoundTag save(MultiBlockData data) {
            var tag = new CompoundTag();
            tag.putInt(SIZE_ID, data.size);
            tag.put(START_POS_ID, NbtUtils.writeBlockPos(data.startPosition));
            tag.putInt(DEPTH_DIR_ID, data.itDirs.depthDirection().ordinal());
            tag.putInt(ROW_DIR_ID, data.itDirs.rowDirection().ordinal());
            tag.putInt(COL_DIR_ID, data.itDirs.columnDirection().ordinal());
            return tag;
        }

        @Nullable
        private static SizeCheckResult findEdge(
            BlockPos fromPos, Direction direction, Predicate<? super BlockPos> edgeCheck
        ) {
            for (var controllerOffset = 1; controllerOffset < MAX_SIZE; controllerOffset++) {
                var curPos = fromPos.relative(direction, controllerOffset);
                if (edgeCheck.test(curPos)) {
                    return new SizeCheckResult(curPos, controllerOffset);
                }
            }
            return null;
        }

        private static BlockPos getStartPosition(BlockPos blockPos, BlockPos negativeRowPos, BlockPos positiveRowPos) {
            var hPos = negativeRowPos.subtract(blockPos);
            var vPos = positiveRowPos.subtract(blockPos);
            return blockPos.offset(hPos.offset(vPos));
        }

        private record SizeCheckResult(BlockPos blockPos, int controllerOffset) {}
    }
}
