package com.github.almostreliable.lazierae2.core;

import com.github.almostreliable.lazierae2.machine.MachineBlock;
import com.github.almostreliable.lazierae2.container.*;
import com.github.almostreliable.lazierae2.tile.*;
import net.minecraft.block.Block;
import net.minecraft.data.BlockStateVariantBuilder.ITriFunction;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public final class Setup {

    private static final Tab TAB = new Tab(MOD_ID);

    private Setup() {}

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Items.REGISTRY.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    public static final class Tiles {

        private static final DeferredRegister<TileEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        private Tiles() {}

        private static <T extends MachineTile, B extends MachineBlock> RegistryObject<TileEntityType<T>> register(
            String id, RegistryObject<B> block, Supplier<T> constructor
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(id, () -> Builder.of(constructor, block.get()).build(null));
        }

        public static final RegistryObject<TileEntityType<AggregatorTile>> AGGREGATOR = register(AGGREGATOR_ID,
            Blocks.AGGREGATOR,
            AggregatorTile::new
        );
        public static final RegistryObject<TileEntityType<CentrifugeTile>> CENTRIFUGE = register(CENTRIFUGE_ID,
            Blocks.CENTRIFUGE,
            CentrifugeTile::new
        );
        public static final RegistryObject<TileEntityType<EnergizerTile>> ENERGIZER = register(ENERGIZER_ID,
            Blocks.ENERGIZER,
            EnergizerTile::new
        );
        public static final RegistryObject<TileEntityType<EtcherTile>> ETCHER = register(ETCHER_ID,
            Blocks.ETCHER,
            EtcherTile::new
        );
    }

    public static final class Containers {

        private static final DeferredRegister<ContainerType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

        private Containers() {}

        private static <C extends MachineContainer> RegistryObject<ContainerType<C>> register(
            String id,
            ITriFunction<? super Integer, ? super MachineTile, ? super PlayerInventory, ? extends C> constructor
        ) {
            return REGISTRY.register(id, () -> IForgeContainerType.create((containerID, inventory, data) -> {
                TileEntity tile = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(tile instanceof MachineTile)) throw new IllegalStateException("Tile is not a LazierAE2 machine!");
                return constructor.apply(containerID, (MachineTile) tile, inventory);
            }));
        }

        public static final RegistryObject<ContainerType<AggregatorContainer>> AGGREGATOR = register(AGGREGATOR_ID,
            AggregatorContainer::new
        );
        public static final RegistryObject<ContainerType<CentrifugeContainer>> CENTRIFUGE = register(CENTRIFUGE_ID,
            CentrifugeContainer::new
        );
        public static final RegistryObject<ContainerType<EnergizerContainer>> ENERGIZER = register(ENERGIZER_ID,
            EnergizerContainer::new
        );
        public static final RegistryObject<ContainerType<EtcherContainer>> ETCHER = register(ETCHER_ID,
            EtcherContainer::new
        );
    }

    private static final class Tab extends ItemGroup {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(net.minecraft.block.Blocks.GRASS_BLOCK);
        }
    }

    public static final class Blocks {

        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<MachineBlock> AGGREGATOR = register(AGGREGATOR_ID, MachineBlock::new, 3);
        public static final RegistryObject<MachineBlock> CENTRIFUGE = register(CENTRIFUGE_ID, MachineBlock::new, 1);
        public static final RegistryObject<MachineBlock> ENERGIZER = register(ENERGIZER_ID, MachineBlock::new, 1);
        public static final RegistryObject<MachineBlock> ETCHER = register(ETCHER_ID, MachineBlock::new, 3);

        private Blocks() {}

        private static <B extends MachineBlock> RegistryObject<B> register(
            String id, Function<? super Integer, ? extends B> constructor, int inputSlots
        ) {
            RegistryObject<B> block = REGISTRY.register(id, () -> constructor.apply(inputSlots));
            Items.REGISTRY.register(id, () -> new BlockItem(block.get(), new Properties().tab(TAB)));
            return block;
        }
    }

    private static class Items {

        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

        private static RegistryObject<Item> register(String id) {
            return REGISTRY.register(id, () -> new Item(new Properties().tab(TAB)));
        }
    }
}
