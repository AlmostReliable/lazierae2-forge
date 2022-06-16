package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.assembler.MultiBlock.PositionType;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerBlock;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PatternHolderBlock extends AssemblerBlock {

    private final HOLDER_TIER tier;

    public PatternHolderBlock(HOLDER_TIER tier) {
        this.tier = tier;
    }

    @Override
    public boolean isValidMultiBlockPos(PositionType posType) {
        return posType == PositionType.INNER;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (isMultiBlock(oldState) && newState.isAir()) {
            var nearestHullPos = findNearestHullPos(level, pos, MultiBlock.MAX_SIZE);
            if (nearestHullPos != null && level.getBlockState(nearestHullPos)
                .getBlock() instanceof HullBlock hullBlock) {
                var controllerPos = hullBlock.findControllerPos(
                    level,
                    nearestHullPos,
                    level.getBlockState(nearestHullPos)
                );
                if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof ControllerEntity entity &&
                    entity.getBlockState().getBlock() instanceof ControllerBlock controller) {
                    controller.destroyMultiBlock(level, entity, pos);
                }
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }

    @Nullable
    private BlockPos findNearestHullPos(Level level, BlockPos pos, int maxStep) {
        if (maxStep == 0) return null;
        var posAbove = pos.above();
        var block = level.getBlockState(posAbove).getBlock();
        if (block instanceof HullBlock) return posAbove;
        return findNearestHullPos(level, posAbove, maxStep - 1);
    }

    public HOLDER_TIER getTier() {
        return tier;
    }

    public enum HOLDER_TIER {
        ACCELERATOR, TIER_1, TIER_2, TIER_3
    }
}
