package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class MultiBlock {
    protected static final int INCLUDED_CONTROLLER_POS = 1;

    public static boolean iterateMultiBlock(
        Data data, IterateCallback callback
    ) {
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.size(); j++) {
                for (int k = 0; k < data.size(); k++) {
                    BlockPos position = data.itDirs().relative(data.startPosition(), i, j, k);
                    Type type = Type.of(i, j, k, data.size());
                    if (!callback.apply(type, position)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected static boolean isCenter(int i, int j, int k, int size) {
        return (0 < i && i < size - 1) && (0 < j && j < size - 1) && (0 < k && k < size - 1);
    }

    protected static boolean isCorner(int i, int j, int k, int size) {
        return (i == 0 || i == size - 1) && (j == 0 || j == size - 1) && (k == 0 || k == size - 1);
    }

    protected static boolean isEdge(int i, int j, int k, int size) {
        boolean iTest = i == 0 || i == size - 1;
        boolean jTest = j == 0 || j == size - 1;
        boolean kTest = k == 0 || k == size - 1;
        return iTest && jTest || iTest && kTest || jTest && kTest;
    }

    protected static BlockPos getStartPosition(BlockPos blockPos, BlockPos negativeRowPos, BlockPos positiveRowPos) {
        BlockPos hPos = negativeRowPos.subtract(blockPos);
        BlockPos vPos = positiveRowPos.subtract(blockPos);
        return blockPos.offset(hPos.offset(vPos));
    }

    @Nullable
    public static SizeCheckResult findEdge(
        BlockPos fromPos, Direction direction, int maxTries, Predicate<BlockPos> edgeCheck
    ) {
        for (int i = 1; i < maxTries; i++) {
            BlockPos curPos = fromPos.relative(direction, i);
            if (edgeCheck.test(curPos)) {
                return new SizeCheckResult(curPos, i);
            }
        }
        return null;
    }

    public enum Type {
        WALL, CORNER, EDGE, CENTER;

        static Type of(int i, int j, int k, int size) {
            if (isCenter(i, j, k, size)) {
                return CENTER;
            } else if (isCorner(i, j, k, size)) {
                return CORNER;
            } else if (isEdge(i, j, k, size)) {
                return EDGE;
            }
            return WALL;
        }
    }

    @FunctionalInterface
    public interface IterateCallback {
        boolean apply(Type type, BlockPos currentPos);
    }

    public record SizeCheckResult(BlockPos blockPos, int size) {}

    public record IterateDirections(Direction mDir, Direction rDir, Direction cDir) {
        public static IterateDirections ofFacing(Direction facing) {
            return switch (facing) {
                case UP -> new IterateDirections(Direction.DOWN, Direction.EAST, Direction.SOUTH);
                case DOWN -> new IterateDirections(Direction.UP, Direction.WEST, Direction.NORTH);
                default -> new IterateDirections(facing.getOpposite(),
                    facing.getOpposite().getClockWise(),
                    Direction.UP
                );
            };
        }

        public BlockPos relative(BlockPos blockPos, int i, int j, int k) {
            int x = blockPos.getX() + (mDir().getStepX() * i) + (rDir().getStepX() * j) + (cDir().getStepX() * k);
            int y = blockPos.getY() + (mDir().getStepY() * i) + (rDir().getStepY() * j) + (cDir().getStepY() * k);
            int z = blockPos.getZ() + (mDir().getStepZ() * i) + (rDir().getStepZ() * j) + (cDir().getStepZ() * k);
            return new BlockPos(x, y, z);
        }
    }

    public record Data(int size, BlockPos startPosition, IterateDirections itDirs) {
        @Nullable
        public static Data of(
            BlockPos originPos, IterateDirections itDirs, int minSize, int maxSize, Predicate<BlockPos> edgeCheck
        ) {
            SizeCheckResult negativeRowResult = findEdge(originPos, itDirs.rDir().getOpposite(), maxSize, edgeCheck);
            SizeCheckResult positiveRowResult = findEdge(originPos, itDirs.rDir(), maxSize, edgeCheck);
            SizeCheckResult negativeColumnResult = findEdge(originPos, itDirs.cDir().getOpposite(), maxSize, edgeCheck);
            SizeCheckResult positiveColumnResult = findEdge(originPos, itDirs.cDir(), maxSize, edgeCheck);

            if (negativeRowResult == null || positiveRowResult == null || negativeColumnResult == null ||
                positiveColumnResult == null) {
                return null;
            }

            int size = negativeRowResult.size() + INCLUDED_CONTROLLER_POS + positiveRowResult.size();
            if (size != negativeColumnResult.size() + INCLUDED_CONTROLLER_POS + positiveColumnResult.size() ||
                size < minSize) {
                return null;
            }

            BlockPos startPosition = getStartPosition(originPos,
                negativeRowResult.blockPos(),
                negativeColumnResult.blockPos()
            );

            return new Data(size, startPosition, itDirs);
        }

        public static Data load(CompoundTag tag) {
            int size = tag.getInt("size");
            BlockPos startPosition = NbtUtils.readBlockPos(tag);
            Direction mDir = Direction.valueOf(tag.getString("mDir"));
            Direction rDir = Direction.valueOf(tag.getString("rDir"));
            Direction cDir = Direction.valueOf(tag.getString("cDir"));
            return new Data(size, startPosition, new IterateDirections(mDir, rDir, cDir));
        }

        public static CompoundTag save(Data data) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("size", data.size);
            NbtUtils.writeBlockPos(data.startPosition);
            tag.putString("mDir", data.itDirs.mDir().toString());
            tag.putString("rDir", data.itDirs.rDir().toString());
            tag.putString("cDir", data.itDirs.cDir().toString());
            return tag;
        }
    }
}
