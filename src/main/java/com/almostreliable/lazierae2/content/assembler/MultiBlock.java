package com.almostreliable.lazierae2.content.assembler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nullable;
import java.util.function.Predicate;

final class MultiBlock {

    private static final int INCLUDED_CONTROLLER_POS = 1;

    private MultiBlock() {}

    static boolean iterateMultiBlock(
        Data data, IterateCallback callback
    ) {
        for (var i = 0; i < data.size(); i++) {
            for (var j = 0; j < data.size(); j++) {
                for (var k = 0; k < data.size(); k++) {
                    var position = data.itDirs().relative(data.startPosition(), i, j, k);
                    var type = Type.of(i, j, k, data.size());
                    if (!callback.apply(type, position)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public enum Type {
        WALL, CORNER, EDGE, CENTER;

        static Type of(int i, int j, int k, int size) {
            if (isCenter(i, j, k, size)) {
                return CENTER;
            }
            if (isCorner(i, j, k, size)) {
                return CORNER;
            }
            if (isEdge(i, j, k, size)) {
                return EDGE;
            }
            return WALL;
        }

        private static boolean isCenter(int i, int j, int k, int size) {
            return i > 0 && i < size - 1 && j > 0 && j < size - 1 && k > 0 && k < size - 1;
        }

        private static boolean isCorner(int i, int j, int k, int size) {
            return (i == 0 || i == size - 1) && (j == 0 || j == size - 1) && (k == 0 || k == size - 1);
        }

        private static boolean isEdge(int i, int j, int k, int size) {
            var iTest = i == 0 || i == size - 1;
            var jTest = j == 0 || j == size - 1;
            var kTest = k == 0 || k == size - 1;
            return iTest && jTest || iTest && kTest || jTest && kTest;
        }
    }

    @FunctionalInterface
    public interface IterateCallback {
        boolean apply(Type type, BlockPos currentPos);
    }

    private record SizeCheckResult(BlockPos blockPos, int size) {}

    record IterateDirections(Direction depthDirection, Direction rowDirection, Direction columnDirection) {

        static IterateDirections ofFacing(Direction facing) {
            if (facing == Direction.UP) {
                return new IterateDirections(Direction.DOWN, Direction.EAST, Direction.SOUTH);
            }
            if (facing == Direction.DOWN) {
                return new IterateDirections(Direction.UP, Direction.WEST, Direction.NORTH);
            }
            return new IterateDirections(facing.getOpposite(), facing.getOpposite().getClockWise(), Direction.UP);
        }

        private BlockPos relative(BlockPos blockPos, int i, int j, int k) {
            var x = calculatePosition(blockPos.getX(),
                depthDirection.getStepX() * i,
                rowDirection.getStepX() * j,
                columnDirection.getStepX() * k
            );
            var y = calculatePosition(blockPos.getY(),
                depthDirection.getStepY() * i,
                rowDirection.getStepY() * j,
                columnDirection.getStepY() * k
            );
            var z = calculatePosition(blockPos.getZ(),
                depthDirection.getStepZ() * i,
                rowDirection.getStepZ() * j,
                columnDirection.getStepZ() * k
            );
            return new BlockPos(x, y, z);
        }

        private int calculatePosition(int initial, int depth, int row, int column) {
            return initial + depth + row + column;
        }
    }

    public record Data(int size, BlockPos startPosition, IterateDirections itDirs) {

        @Nullable
        public static Data of(
            BlockPos originPos, IterateDirections itDirs, int minSize, int maxSize,
            Predicate<? super BlockPos> edgeCheck
        ) {
            var negativeRowResult = findEdge(originPos, itDirs.rowDirection().getOpposite(), maxSize, edgeCheck);
            var positiveRowResult = findEdge(originPos, itDirs.rowDirection(), maxSize, edgeCheck);
            var negativeColumnResult = findEdge(originPos, itDirs.columnDirection().getOpposite(), maxSize, edgeCheck);
            var positiveColumnResult = findEdge(originPos, itDirs.columnDirection(), maxSize, edgeCheck);

            if (negativeRowResult == null || positiveRowResult == null || negativeColumnResult == null ||
                positiveColumnResult == null) {
                return null;
            }

            var size = negativeRowResult.size() + INCLUDED_CONTROLLER_POS + positiveRowResult.size();
            if (size != negativeColumnResult.size() + INCLUDED_CONTROLLER_POS + positiveColumnResult.size() ||
                size < minSize) {
                return null;
            }

            var startPosition = getStartPosition(originPos,
                negativeRowResult.blockPos(),
                negativeColumnResult.blockPos()
            );

            return new Data(size, startPosition, itDirs);
        }

        static Data load(CompoundTag tag) {
            var size = tag.getInt("size");
            var startPosition = NbtUtils.readBlockPos(tag);
            var mDir = Direction.valueOf(tag.getString("depthDirection"));
            var rDir = Direction.valueOf(tag.getString("rowDirection"));
            var cDir = Direction.valueOf(tag.getString("columnDirection"));
            return new Data(size, startPosition, new IterateDirections(mDir, rDir, cDir));
        }

        static CompoundTag save(Data data) {
            var tag = new CompoundTag();
            tag.putInt("size", data.size);
            NbtUtils.writeBlockPos(data.startPosition);
            tag.putString("depthDirection", data.itDirs.depthDirection().toString());
            tag.putString("rowDirection", data.itDirs.rowDirection().toString());
            tag.putString("columnDirection", data.itDirs.columnDirection().toString());
            return tag;
        }

        @Nullable
        private static SizeCheckResult findEdge(
            BlockPos fromPos, Direction direction, int maxTries, Predicate<? super BlockPos> edgeCheck
        ) {
            for (var i = 1; i < maxTries; i++) {
                var curPos = fromPos.relative(direction, i);
                if (edgeCheck.test(curPos)) {
                    return new SizeCheckResult(curPos, i);
                }
            }
            return null;
        }

        private static BlockPos getStartPosition(BlockPos blockPos, BlockPos negativeRowPos, BlockPos positiveRowPos) {
            var hPos = negativeRowPos.subtract(blockPos);
            var vPos = positiveRowPos.subtract(blockPos);
            return blockPos.offset(hPos.offset(vPos));
        }
    }
}
