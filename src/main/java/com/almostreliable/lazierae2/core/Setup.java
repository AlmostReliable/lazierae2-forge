package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.assembler.AssemblerFrameBlock;
import com.almostreliable.lazierae2.content.assembler.AssemblerWallBlock;
import com.almostreliable.lazierae2.content.assembler.ControllerBlock;
import com.almostreliable.lazierae2.content.assembler.ControllerEntity;
import com.almostreliable.lazierae2.content.machine.MachineBlock;
import com.almostreliable.lazierae2.content.machine.MachineEntity;
import com.almostreliable.lazierae2.content.machine.MachineMenu;
import com.almostreliable.lazierae2.content.machine.MachineType;
import com.almostreliable.lazierae2.content.maintainer.MaintainerBlock;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe.MachineRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.*;

public final class Setup {

    private static final Tab TAB = new Tab(MOD_ID);

    private Setup() {}

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Items.REGISTRY.register(modEventBus);
        Entities.REGISTRY.register(modEventBus);
        Menus.REGISTRY.register(modEventBus);
        Serializers.REGISTRY.register(modEventBus);
    }

    public static final class Entities {

        private static final DeferredRegister<BlockEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MOD_ID);

        private Entities() {}

        @SafeVarargs
        private static <E extends GenericEntity, B extends GenericBlock> RegistryObject<BlockEntityType<E>> register(
            String id, BlockEntitySupplier<E> entity, RegistryObject<B>... blocks
        ) {
            // noinspection ConstantConditions
            return REGISTRY.register(id,
                () -> Builder
                    .of(entity, Arrays.stream(blocks).map(RegistryObject::get).toArray(GenericBlock[]::new))
                    .build(null)
            );
        }

        public static final RegistryObject<BlockEntityType<ControllerEntity>> CONTROLLER = REGISTRY.register("controller_block",
            () -> Builder.of(ControllerEntity::new, Blocks.CONTROLLER_BLOCK.get()).build(null)
        );

        public static final RegistryObject<BlockEntityType<MaintainerEntity>> MAINTAINER = register(MAINTAINER_ID,
            MaintainerEntity::new,
            Blocks.MAINTAINER
        );

        public static final RegistryObject<BlockEntityType<MachineEntity>> MACHINE = register(MACHINE_ID,
            MachineEntity::new,
            Blocks.AGGREGATOR,
            Blocks.CENTRIFUGE,
            Blocks.ENERGIZER,
            Blocks.ETCHER
        );
    }

    public static final class Menus {

        private static final DeferredRegister<MenuType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

        public static final RegistryObject<MenuType<MachineMenu>> MACHINE = register(MACHINE_ID,
            (windowId, inventory, data) -> {
                var entity = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(entity instanceof MachineEntity machine)) {
                    throw new IllegalStateException("Tile is not a LazierAE2 machine!");
                }
                return new MachineMenu(windowId, machine, inventory);
            }
        );

        public static final RegistryObject<MenuType<MaintainerMenu>> MAINTAINER = register(MAINTAINER_ID,
            (windowId, inventory, data) -> {
                var entity = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(entity instanceof MaintainerEntity maintainer)) {
                    throw new IllegalStateException("Tile is not a LazierAE2 maintainer!");
                }
                return new MaintainerMenu(windowId, maintainer, inventory);
            }
        );

        private Menus() {}

        private static <M extends GenericMenu<?>> RegistryObject<MenuType<M>> register(
            String id, IContainerFactory<M> factory
        ) {
            return REGISTRY.register(id, () -> IForgeMenuType.create(factory));
        }
    }

    private static final class Tab extends CreativeModeTab {

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

        public static final RegistryObject<ControllerBlock> CONTROLLER_BLOCK = REGISTRY.register("controller_block",
            () -> new ControllerBlock(BlockBehaviour.Properties.of(Material.STONE))
        );

        public static final RegistryObject<AssemblerWallBlock> ASSEMBLER_WALL_BLOCK = REGISTRY.register("valid_wall_block",
            () -> new AssemblerWallBlock(BlockBehaviour.Properties.of(Material.STONE))
        );

        public static final RegistryObject<AssemblerFrameBlock> ASSEMBLER_FRAME_BLOCK = REGISTRY.register("valid_edge_block",
            () -> new AssemblerFrameBlock(BlockBehaviour.Properties.of(Material.STONE))
        );

        public static final RegistryObject<MaintainerBlock> MAINTAINER = register(MAINTAINER_ID, MaintainerBlock::new);

        private Blocks() {}

        @SuppressWarnings("SameParameterValue")
        private static <B extends GenericBlock> RegistryObject<B> register(
            String id, Supplier<? extends B> constructor
        ) {
            RegistryObject<B> block = REGISTRY.register(id, constructor);
            registerBlockItem(id, block);
            return block;
        }

        private static <B extends GenericBlock> RegistryObject<B> register(
            Function<? super MachineType, ? extends B> constructor, MachineType machineType
        ) {
            RegistryObject<B> block = REGISTRY.register(machineType.getId(), () -> constructor.apply(machineType));
            registerBlockItem(machineType.getId(), block);
            return block;
        }

        private static <B extends GenericBlock> void registerBlockItem(String id, RegistryObject<B> block) {
            Items.REGISTRY.register(id, () -> new BlockItem(block.get(), new Properties().tab(TAB)));
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

        public static final RegistryObject<BlockItem> CONTROLLER_BLOCK = REGISTRY.register("controller_block",
            () -> new BlockItem(Blocks.CONTROLLER_BLOCK.get(), new Item.Properties().tab(TAB))
        );

        // TODO remove valid wall block item as it should be not possible to create it by user
        public static final RegistryObject<BlockItem> VALID_WALL_BLOCK = REGISTRY.register("valid_wall_block",
            () -> new BlockItem(Blocks.ASSEMBLER_WALL_BLOCK.get(), new Properties().tab(TAB))
        );

        public static final RegistryObject<BlockItem> VALID_EDGE_BLOCK = REGISTRY.register("valid_edge_block",
            () -> new BlockItem(Blocks.ASSEMBLER_FRAME_BLOCK.get(), new Properties().tab(TAB))
        );

        private Items() {}

        private static RegistryObject<Item> register(String id) {
            return REGISTRY.register(id, () -> new Item(new Properties().tab(TAB)));
        }
    }

    public static final class Tags {

        private Tags() {}

        public static final class Items {
            public static final TagKey<Item> DUSTS_COAL = forge("dusts/coal");
            public static final TagKey<Item> DUSTS_CARBONIC_FLUIX = forge("dusts/carbonic_fluix");
            public static final TagKey<Item> GEMS_RESONATING = forge("gems/resonating");
            public static final TagKey<Item> INGOTS_FLUIX_IRON = forge("ingots/fluix_iron");
            public static final TagKey<Item> INGOTS_FLUIX_STEEL = forge("ingots/fluix_steel");

            public static final TagKey<Item> PROCESSOR_PARALLEL = mod("processors/parallel");
            public static final TagKey<Item> PROCESSOR_SPEC = mod("processors/speculative");

            // Applied Energistics 2
            public static final TagKey<Item> SILICON = forge("silicon");

            private Items() {}

            private static TagKey<Item> forge(String path) {
                return ItemTags.create(new ResourceLocation("forge", path));
            }

            private static TagKey<Item> mod(String path) {
                return ItemTags.create(new ResourceLocation(MOD_ID, path));
            }
        }

        public static final class Blocks {

            private static final String MACHINE_ENTRY = "machines/";
            public static final TagKey<Block> AGGREGATOR = mod(MACHINE_ENTRY + MachineType.AGGREGATOR.getId());
            public static final TagKey<Block> CENTRIFUGE = mod(MACHINE_ENTRY + MachineType.CENTRIFUGE.getId());
            public static final TagKey<Block> ENERGIZER = mod(MACHINE_ENTRY + MachineType.ENERGIZER.getId());
            public static final TagKey<Block> ETCHER = mod(MACHINE_ENTRY + MachineType.ETCHER.getId());

            private Blocks() {}

            private static TagKey<Block> mod(String path) {
                return BlockTags.create(new ResourceLocation(MOD_ID, path));
            }
        }
    }

    public static final class Recipes {

        private Recipes() {}

        public static final class Serializers {

            private static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,
                MOD_ID
            );

            public static final RegistryObject<RecipeSerializer<MachineRecipe>> AGGREGATOR
                = register(MachineType.AGGREGATOR);
            public static final RegistryObject<RecipeSerializer<MachineRecipe>> CENTRIFUGE
                = register(MachineType.CENTRIFUGE);
            public static final RegistryObject<RecipeSerializer<MachineRecipe>> ENERGIZER
                = register(MachineType.ENERGIZER);
            public static final RegistryObject<RecipeSerializer<MachineRecipe>> ETCHER = register(MachineType.ETCHER);

            private Serializers() {}

            private static RegistryObject<RecipeSerializer<MachineRecipe>> register(
                MachineType machineType
            ) {
                return REGISTRY.register(machineType.getId(), () -> new MachineRecipeSerializer(machineType));
            }
        }
    }
}
