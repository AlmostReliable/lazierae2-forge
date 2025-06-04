package com.almostreliable.lazierae.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae.blockentity.ProcessingMachineBlockEntity;
import com.almostreliable.lazierae.util.EnergyGeneratorBlock;
import com.almostreliable.lib.registry.BlockEntityEntry;
import com.almostreliable.lib.registry.BlockEntityRegistration;
import com.almostreliable.lib.registry.BlockEntry;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;

import java.util.List;
import java.util.function.BiFunction;

public final class LazierBlockEntities {

    private static final BlockEntityRegistration REGISTRY = LazierAE2.MANAGER.blockEntities();

    //@formatter:off

    // machines
    public static final BlockEntityEntry<CrystalInjectorBlockEntity> INJECTOR = register("crystal_injector", CrystalInjectorBlockEntity::new, CrystalInjectorBlockEntity.class, LazierBlocks.INJECTOR);
    public static final BlockEntityEntry<ProcessingMachineBlockEntity> PROCESSING_MACHINE = register("processing_machine", ProcessingMachineBlockEntity::new, ProcessingMachineBlockEntity.class, LazierBlocks.ETCHER, LazierBlocks.INFUSER);

    // dev
    public static final BlockEntityEntry<EnergyGeneratorBlock.Entity> ENERGY_GENERATOR_ENTITY = REGISTRY.builder("energy_generator", EnergyGeneratorBlock.Entity::new).block(LazierBlocks.ENERGY_GENERATOR).register();

    //@formatter:on

    private LazierBlockEntities() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing block entities");
    }

    @SafeVarargs
    private static <BE extends AEBaseBlockEntity, B extends AEBaseEntityBlock<BE>> BlockEntityEntry<BE> register(
        String id, BiFunction<BlockPos, BlockState, BE> factory, Class<BE> blockEntityClass, BlockEntry<B>... blockEntries
    ) {
        var entry = REGISTRY.builder(id, factory).blocks(() -> List.of(blockEntries)).register();

        for (var blockEntry : blockEntries) {
            blockEntry.get().setBlockEntity(blockEntityClass, entry.get(), null, null);
        }

        return entry;
    }
}
