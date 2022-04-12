package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.almostreliable.lazierae2.machine.MachineBlock;
import com.almostreliable.lazierae2.machine.MachineContainer;
import com.almostreliable.lazierae2.machine.MachineTile;
import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe.MachineRecipeSerializer;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

import static com.almostreliable.lazierae2.core.Constants.*;

public final class Setup {

    private static final Tab TAB = new Tab(MOD_ID);

    private Setup() {}

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Items.REGISTRY.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
        Serializers.REGISTRY.register(modEventBus);
    }

    public static final class Tiles {

        private static final DeferredRegister<TileEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        private Tiles() {}

        @SuppressWarnings("ConstantConditions")
        public static final RegistryObject<TileEntityType<MachineTile>> MACHINE = REGISTRY.register(MACHINE_ID,
            () -> Builder.of(MachineTile::new,
                Blocks.AGGREGATOR.get(),
                Blocks.CENTRIFUGE.get(),
                Blocks.ENERGIZER.get(),
                Blocks.ETCHER.get()
            ).build(null)
        );
    }

    public static final class Containers {

        private static final DeferredRegister<ContainerType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
        public static final RegistryObject<ContainerType<MachineContainer>> MACHINE = REGISTRY.register(MACHINE_ID,
            () -> IForgeContainerType.create((containerID, inventory, data) -> {
                TileEntity tile = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(tile instanceof MachineTile)) throw new IllegalStateException("Tile is not a LazierAE2 machine!");
                return new MachineContainer(containerID, (MachineTile) tile, inventory);
            })
        );

        private Containers() {}
    }

    private static final class Tab extends ItemGroup {

        private Tab(String label) {
            super(label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.AGGREGATOR.get());
        }
    }

    public static final class Blocks {

        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        public static final RegistryObject<MachineBlock> AGGREGATOR = register(MachineBlock::new,
            MachineType.AGGREGATOR
        );
        public static final RegistryObject<MachineBlock> CENTRIFUGE = register(MachineBlock::new,
            MachineType.CENTRIFUGE
        );
        public static final RegistryObject<MachineBlock> ENERGIZER = register(MachineBlock::new, MachineType.ENERGIZER);
        public static final RegistryObject<MachineBlock> ETCHER = register(MachineBlock::new, MachineType.ETCHER);

        private Blocks() {}

        private static <B extends MachineBlock> RegistryObject<B> register(
            Function<? super MachineType, ? extends B> constructor, MachineType machineType
        ) {
            RegistryObject<B> block = REGISTRY.register(machineType.getId(), () -> constructor.apply(machineType));
            Items.REGISTRY.register(machineType.getId(), () -> new BlockItem(block.get(), new Properties().tab(TAB)));
            return block;
        }
    }

    public static final class Items {

        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
        public static final RegistryObject<Item> CARB_FLUIX_DUST = register(CARB_FLUIX_DUST_ID);
        public static final RegistryObject<Item> COAL_DUST = register(COAL_DUST_ID);
        public static final RegistryObject<Item> FLUIX_IRON = register(FLUIX_IRON_ID);
        public static final RegistryObject<Item> FLUIX_STEEL = register(FLUIX_STEEL_ID);
        public static final RegistryObject<Item> GROWTH_CHAMBER = register(GROWTH_CHAMBER_ID);
        public static final RegistryObject<Item> LOGIC_UNIT = register(LOGIC_UNIT_ID);
        public static final RegistryObject<Item> PARALLEL_PRINTED = register(PARALLEL_PRINTED_ID);
        public static final RegistryObject<Item> PARALLEL_PROCESSOR = register(PARALLEL_PROCESSOR_ID);
        public static final RegistryObject<Item> RESONATING_GEM = register(RESONATING_GEM_ID);
        public static final RegistryObject<Item> SPEC_CORE_1 = register(SPEC_CORE_1_ID);
        public static final RegistryObject<Item> SPEC_CORE_2 = register(SPEC_CORE_2_ID);
        public static final RegistryObject<Item> SPEC_CORE_4 = register(SPEC_CORE_4_ID);
        public static final RegistryObject<Item> SPEC_CORE_8 = register(SPEC_CORE_8_ID);
        public static final RegistryObject<Item> SPEC_CORE_16 = register(SPEC_CORE_16_ID);
        public static final RegistryObject<Item> SPEC_CORE_32 = register(SPEC_CORE_32_ID);
        public static final RegistryObject<Item> SPEC_CORE_64 = register(SPEC_CORE_64_ID);
        public static final RegistryObject<Item> SPEC_PRINTED = register(SPEC_PRINTED_ID);
        public static final RegistryObject<Item> SPEC_PROCESSOR = register(SPEC_PROCESSOR_ID);
        public static final RegistryObject<Item> UNIVERSAL_PRESS = register(UNIVERSAL_PRESS_ID);

        private Items() {}

        private static RegistryObject<Item> register(String id) {
            return REGISTRY.register(id, () -> new Item(new Properties().tab(TAB)));
        }
    }

    public static final class Tags {

        private Tags() {}

        public static final class Items {
            public static final INamedTag<Item> DUSTS_COAL = forge("dusts/coal");
            public static final INamedTag<Item> DUSTS_CARBONIC_FLUIX = forge("dusts/carbonic_fluix");
            public static final INamedTag<Item> GEMS_RESONATING = forge("gems/resonating");
            public static final INamedTag<Item> INGOTS_FLUIX_IRON = forge("ingots/fluix_iron");
            public static final INamedTag<Item> INGOTS_FLUIX_STEEL = forge("ingots/fluix_steel");

            public static final INamedTag<Item> PROCESSOR_PARALLEL = mod("processors/parallel");
            public static final INamedTag<Item> PROCESSOR_SPEC = mod("processors/speculative");

            // Applied Energistics 2
            public static final INamedTag<Item> SILICON = ItemTags.bind("forge:silicon");

            private Items() {}

            private static INamedTag<Item> forge(String path) {
                return ItemTags.bind(new ResourceLocation("forge", path).toString());
            }

            private static INamedTag<Item> mod(String path) {
                return ItemTags.bind(new ResourceLocation(MOD_ID, path).toString());
            }
        }

        public static final class Blocks {

            private static final String MACHINE_ENTRY = "machines/";
            public static final INamedTag<Block> AGGREGATOR = mod(MACHINE_ENTRY + MachineType.AGGREGATOR.getId());
            public static final INamedTag<Block> CENTRIFUGE = mod(MACHINE_ENTRY + MachineType.CENTRIFUGE.getId());
            public static final INamedTag<Block> ENERGIZER = mod(MACHINE_ENTRY + MachineType.ENERGIZER.getId());
            public static final INamedTag<Block> ETCHER = mod(MACHINE_ENTRY + MachineType.ETCHER.getId());

            private Blocks() {}

            private static INamedTag<Block> mod(String path) {
                return BlockTags.bind(new ResourceLocation(MOD_ID, path).toString());
            }
        }
    }

    public static final class Recipes {

        private Recipes() {}

        public static final class Serializers {

            private static final DeferredRegister<IRecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,
                MOD_ID
            );

            public static final RegistryObject<IRecipeSerializer<MachineRecipe>> AGGREGATOR
                = register(MachineType.AGGREGATOR);
            public static final RegistryObject<IRecipeSerializer<MachineRecipe>> CENTRIFUGE
                = register(MachineType.CENTRIFUGE);
            public static final RegistryObject<IRecipeSerializer<MachineRecipe>> ENERGIZER
                = register(MachineType.ENERGIZER);
            public static final RegistryObject<IRecipeSerializer<MachineRecipe>> ETCHER = register(MachineType.ETCHER);

            private Serializers() {}

            private static RegistryObject<IRecipeSerializer<MachineRecipe>> register(
                MachineType machineType
            ) {
                return REGISTRY.register(machineType.getId(), () -> new MachineRecipeSerializer(machineType));
            }
        }
    }
}
