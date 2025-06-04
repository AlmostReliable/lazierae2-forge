package com.almostreliable.lazierae.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.registry.LazierBlockEntities;
import com.almostreliable.lib.Services;
import com.almostreliable.lib.block.TickableEntityBlock;
import com.almostreliable.lib.util.Tickable;

import org.jetbrains.annotations.Nullable;

// TODO: remove on release
public class EnergyGeneratorBlock extends Block implements TickableEntityBlock {

    public EnergyGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    public static class Entity extends BlockEntity implements Tickable {

        private static final long ENERGY_GEN_RATE = 100;

        public Entity(BlockPos blockPos, BlockState blockState) {
            super(LazierBlockEntities.ENERGY_GENERATOR_ENTITY.get(), blockPos, blockState);
        }

        @Override
        public void serverTick() {
            if (level == null) return;

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = getBlockPos().relative(direction);
                var energyContainer = Services.COMPONENTS.findEnergyContainer(level, neighborPos, direction.getOpposite());

                if (energyContainer == null) continue;

                energyContainer.insert(ENERGY_GEN_RATE, false);
            }
        }
    }
}
