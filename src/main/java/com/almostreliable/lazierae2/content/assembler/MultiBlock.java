package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static com.almostreliable.lazierae2.LazierAE2.LOG;
import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public final class MultiBlock {

    static final int MAX_SIZE = 32;
    private static final int MIN_SIZE = 4;
    private static final int MAX_VOLUME = 1_000;

    private MultiBlock() {}

    public static boolean iterateMultiBlock(
        MultiBlockData data, IterateCallback callback
    ) {
        for (var pos : BlockPos.betweenClosed(
            data.startPosition.getX(),
            data.startPosition.getY(),
            data.startPosition.getZ(),
            data.endPosition.getX(),
            data.endPosition.getY(),
            data.endPosition.getZ()
        )) {
            var immutablePos = pos.immutable();
            var posType = data.getPositionType(immutablePos);
            if (!callback.apply(posType, immutablePos)) {
                return false;
            }
        }
        return true;
    }

    public enum PositionType {
        WALL, CORNER, EDGE, INNER
    }

    @FunctionalInterface
    public interface IterateCallback {
        boolean apply(PositionType posType, BlockPos pos);
    }

    public record IterateDirections(Direction depthDirection, Direction rowDirection, Direction columnDirection) {
        public static IterateDirections of(Direction facing) {
            if (facing == Direction.UP) {
                return new IterateDirections(Direction.DOWN, Direction.EAST, Direction.SOUTH);
            }
            if (facing == Direction.DOWN) {
                return new IterateDirections(Direction.UP, Direction.WEST, Direction.NORTH);
            }
            return new IterateDirections(facing.getOpposite(), facing.getOpposite().getClockWise(), Direction.UP);
        }
    }

    public static final class MultiBlockData {

        private final BlockPos startPosition;
        private final BlockPos endPosition;
        private final IterateDirections itDirs;

        private MultiBlockData(BlockPos startPosition, BlockPos endPosition, IterateDirections itDirs) {
            this.startPosition = new BlockPos(
                Math.min(startPosition.getX(), endPosition.getX()),
                Math.min(startPosition.getY(), endPosition.getY()),
                Math.min(startPosition.getZ(), endPosition.getZ())
            );

            this.endPosition = new BlockPos(
                Math.max(startPosition.getX(), endPosition.getX()),
                Math.max(startPosition.getY(), endPosition.getY()),
                Math.max(startPosition.getZ(), endPosition.getZ())
            );

            this.itDirs = itDirs;
        }

        @Nullable
        public static MultiBlockData of(
            BlockPos originPos, IterateDirections itDirs, Predicate<? super BlockPos> edgeCheck,
            Predicate<? super BlockPos> depthCheck, Player player
        ) {
            var negativeRowResult = findEdge(originPos, itDirs.rowDirection().getOpposite(), edgeCheck);
            var positiveRowResult = findEdge(originPos, itDirs.rowDirection(), edgeCheck);
            var negativeColumnResult = findEdge(originPos, itDirs.columnDirection().getOpposite(), edgeCheck);
            var positiveColumnResult = findEdge(originPos, itDirs.columnDirection(), edgeCheck);
            var depthResult = findEdge(originPos, itDirs.depthDirection(), depthCheck);

            if (negativeRowResult == null || positiveRowResult == null || negativeColumnResult == null || positiveColumnResult == null || depthResult == null) {
                GameUtil.sendPlayerMessage(player, "not_found", ChatFormatting.YELLOW);
                LOG.debug("Couldn't determine multiblock shape");
                return null;
            }

            var startPosition = getStartPosition(
                originPos,
                negativeRowResult.blockPos(),
                negativeColumnResult.blockPos()
            );
            var endPosition = getStartPosition(
                originPos,
                positiveRowResult.blockPos(),
                positiveColumnResult.blockPos()
            ).offset(itDirs.depthDirection().getNormal().multiply(depthResult.controllerOffset()));

            var data = new MultiBlockData(startPosition, endPosition, itDirs);
            var size = data.getSize();
            if (size.getX() < MIN_SIZE || size.getY() < MIN_SIZE || size.getZ() < MIN_SIZE) {
                GameUtil.sendPlayerMessage(player, "too_small", ChatFormatting.DARK_RED, MIN_SIZE);
                LOG.debug("MultiBlock too small");
                return null;
            }
            if (size.getX() > MAX_SIZE || size.getY() > MAX_SIZE || size.getZ() > MAX_SIZE) {
                GameUtil.sendPlayerMessage(player, "too_big", ChatFormatting.DARK_RED, MAX_SIZE);
                LOG.debug("MultiBlock too big");
                return null;
            }
            if (size.getX() * size.getY() * size.getZ() > MAX_VOLUME) {
                GameUtil.sendPlayerMessage(player, "too_large", ChatFormatting.DARK_RED, MAX_VOLUME);
                LOG.debug("MultiBlock too large");
                return null;
            }
            return data;
        }

        public static MultiBlockData load(CompoundTag tag) {
            var startPosition = NbtUtils.readBlockPos(tag.getCompound(START_POS_ID));
            var endPosition = NbtUtils.readBlockPos(tag.getCompound(END_POS_ID));
            var mDir = Direction.values()[tag.getInt(DEPTH_DIR_ID)];
            var rDir = Direction.values()[tag.getInt(ROW_DIR_ID)];
            var cDir = Direction.values()[tag.getInt(COL_DIR_ID)];
            return new MultiBlockData(startPosition, endPosition, new IterateDirections(mDir, rDir, cDir));
        }

        public static CompoundTag save(MultiBlockData data) {
            var tag = new CompoundTag();
            tag.put(START_POS_ID, NbtUtils.writeBlockPos(data.startPosition));
            tag.put(END_POS_ID, NbtUtils.writeBlockPos(data.endPosition));
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

        private boolean isInner(BlockPos currentPos) {
            return currentPos.getX() > startPosition.getX() && currentPos.getX() < endPosition.getX()
                && currentPos.getY() > startPosition.getY() && currentPos.getY() < endPosition.getY()
                && currentPos.getZ() > startPosition.getZ() && currentPos.getZ() < endPosition.getZ();
        }

        private boolean isCorner(BlockPos currentPos) {
            return (currentPos.getX() == startPosition.getX() || currentPos.getX() == endPosition.getX())
                && (currentPos.getY() == startPosition.getY() || currentPos.getY() == endPosition.getY())
                && (currentPos.getZ() == startPosition.getZ() || currentPos.getZ() == endPosition.getZ());
        }

        private boolean isEdge(BlockPos currentPos) {
            var iTest = currentPos.getX() == Math.min(
                startPosition.getX(),
                endPosition.getX()
            ) || currentPos.getX() == Math.max(
                startPosition.getX(),
                endPosition.getX()
            );
            var jTest = currentPos.getY() == Math.min(
                startPosition.getY(),
                endPosition.getY()
            ) || currentPos.getY() == Math.max(
                startPosition.getY(),
                endPosition.getY()
            );
            var kTest = currentPos.getZ() == Math.min(
                startPosition.getZ(),
                endPosition.getZ()
            ) || currentPos.getZ() == Math.max(
                startPosition.getZ(),
                endPosition.getZ()
            );
            return iTest && jTest || iTest && kTest || jTest && kTest;
        }

        private PositionType getPositionType(BlockPos currentPos) {
            if (isInner(currentPos)) {
                return PositionType.INNER;
            }
            if (isCorner(currentPos)) {
                return PositionType.CORNER;
            }
            if (isEdge(currentPos)) {
                return PositionType.EDGE;
            }
            return PositionType.WALL;
        }

        public Vec3i getSize() {
            return new Vec3i(
                Math.abs(endPosition.getX() - startPosition.getX()) + 1,
                Math.abs(endPosition.getY() - startPosition.getY()) + 1,
                Math.abs(endPosition.getZ() - startPosition.getZ()) + 1
            );
        }

        private record SizeCheckResult(BlockPos blockPos, int controllerOffset) {}
    }
}
