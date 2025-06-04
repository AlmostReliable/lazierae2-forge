package com.almostreliable.lazierae.block.resonating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import com.almostreliable.lazierae.registry.LazierBlocks;

import appeng.decorative.solid.BuddingCertusQuartzBlock;

public class BuddingResonatingBlock extends BuddingCertusQuartzBlock {

    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingResonatingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(GROWTH_CHANCE) != 0) return;

        Direction direction = DIRECTIONS[randomSource.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        Block newCluster = null;
        if (canClusterGrowAtState(targetState)) {
            newCluster = LazierBlocks.SMALL_RESONATING_BUD.get();
        } else if (targetState.is(LazierBlocks.SMALL_RESONATING_BUD.get()) && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = LazierBlocks.MEDIUM_RESONATING_BUD.get();
        } else if (targetState.is(LazierBlocks.MEDIUM_RESONATING_BUD.get()) && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = LazierBlocks.LARGE_RESONATING_BUD.get();
        } else if (targetState.is(LazierBlocks.LARGE_RESONATING_BUD.get()) && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = LazierBlocks.RESONATING_CLUSTER.get();
        }
        if (newCluster == null) return;

        BlockState newClusterState = newCluster.defaultBlockState()
            .setValue(AmethystClusterBlock.FACING, direction)
            .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().is(Fluids.WATER));
        level.setBlockAndUpdate(targetPos, newClusterState);

        if (randomSource.nextInt(DECAY_CHANCE) != 0) return;

        Block newBlock;
        if (equals(LazierBlocks.FLAWED_BUDDING_RESONATING.get())) {
            newBlock = LazierBlocks.CHIPPED_BUDDING_RESONATING.get();
        } else if (equals(LazierBlocks.CHIPPED_BUDDING_RESONATING.get())) {
            newBlock = LazierBlocks.DAMAGED_BUDDING_RESONATING.get();
        } else if (equals(LazierBlocks.DAMAGED_BUDDING_RESONATING.get())) {
            newBlock = LazierBlocks.RESONATING_BLOCK.get();
        } else {
            throw new IllegalStateException("Unexpected block: " + this);
        }
        level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
    }
}
