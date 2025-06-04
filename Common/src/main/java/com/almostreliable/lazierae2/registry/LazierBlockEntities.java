package com.almostreliable.lazierae2.registry;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lib.registry.BlockEntityRegistration;
import com.almostreliable.lib.registry.BlockEntry;
import com.almostreliable.lib.registry.RegistryEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public final class LazierBlockEntities {

    private static final BlockEntityRegistration REGISTRY = LazierAE2.MANAGER.blockEntities();

    //@formatter:off

    // machines
    public static final RegistryEntry<BlockEntityType<CrystalInjectorBlockEntity>> INJECTOR = register("crystal_injector", CrystalInjectorBlockEntity::new, CrystalInjectorBlockEntity.class, LazierBlocks.INJECTOR);

    //@formatter:on

    private LazierBlockEntities() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing block entities");
    }

    private static <T extends AEBaseBlockEntity, B extends AEBaseEntityBlock<T>> RegistryEntry<BlockEntityType<T>> register(
        String id, BiFunction<BlockPos, BlockState, T> factory, Class<T> blockEntityClass, BlockEntry<B> blockEntry
    ) {
        var entry = REGISTRY.builder(id, factory).block(blockEntry).register();

        BlockEntityTicker<T> clientTicker = null;
        if (ClientTickingBlockEntity.class.isAssignableFrom(blockEntityClass)) {
            clientTicker = (level, pos, state, entity) -> ((ClientTickingBlockEntity) entity).clientTick();
        }
        BlockEntityTicker<T> serverTicker = null;
        if (ServerTickingBlockEntity.class.isAssignableFrom(blockEntityClass)) {
            serverTicker = (level, pos, state, entity) -> ((ServerTickingBlockEntity) entity).serverTick();
        }
        blockEntry.get().setBlockEntity(blockEntityClass, entry.get(), clientTicker, serverTicker);

        return entry;
    }
}
