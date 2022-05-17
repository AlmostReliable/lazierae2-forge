package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.assembler.*;
import com.almostreliable.lazierae2.content.maintainer.MaintainerBlock;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.content.processor.ProcessorBlock;
import com.almostreliable.lazierae2.content.processor.ProcessorEntity;
import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.almostreliable.lazierae2.core.TypeEnums.CENTER_TYPE;
import com.almostreliable.lazierae2.core.TypeEnums.HULL_TYPE;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe.ProcessorRecipeSerializer;
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
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.Blocks.*;
import static com.almostreliable.lazierae2.core.Constants.Items.*;
import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

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

        static {
            Assembler.init();
        }

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

        public static final class Assembler {

            private Assembler() {}

            private static void init() {
                // fake init
            }

            public static final RegistryObject<BlockEntityType<CenterEntity>> CENTER = register(CENTER_ID,
                CenterEntity::new,
                Blocks.Assembler.ACCELERATOR,
                Blocks.Assembler.TIER_1,
                Blocks.Assembler.TIER_2,
                Blocks.Assembler.TIER_3
            );

            public static final RegistryObject<BlockEntityType<ControllerEntity>> ASSEMBLER_CONTROLLER = register(CONTROLLER_ID,
                ControllerEntity::new,
                Blocks.Assembler.CONTROLLER
            );
        }

        public static final RegistryObject<BlockEntityType<ProcessorEntity>> PROCESSOR = register(PROCESSOR_ID,
            ProcessorEntity::new,
            Blocks.AGGREGATOR,
            Blocks.CENTRIFUGE,
            Blocks.ENERGIZER,
            Blocks.ETCHER
        );

        public static final RegistryObject<BlockEntityType<MaintainerEntity>> MAINTAINER = register(MAINTAINER_ID,
            MaintainerEntity::new,
            Blocks.MAINTAINER
        );
    }

    public static final class Menus {

        private static final DeferredRegister<MenuType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

        public static final RegistryObject<MenuType<ProcessorMenu>> PROCESSOR = register(PROCESSOR_ID,
            (windowId, inventory, data) -> {
                var entity = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(entity instanceof ProcessorEntity processor)) {
                    throw new IllegalStateException("Entity is not a LazierAE2 processor!");
                }
                return new ProcessorMenu(windowId, processor, inventory);
            }
        );

        public static final RegistryObject<MenuType<MaintainerMenu>> MAINTAINER = register(MAINTAINER_ID,
            (windowId, inventory, data) -> {
                var entity = inventory.player.level.getBlockEntity(data.readBlockPos());
                if (!(entity instanceof MaintainerEntity maintainer)) {
                    throw new IllegalStateException("Entity is not a LazierAE2 maintainer!");
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

        public static final RegistryObject<ProcessorBlock> AGGREGATOR = register(ProcessorBlock::new,
            ProcessorType.AGGREGATOR
        );
        public static final RegistryObject<ProcessorBlock> CENTRIFUGE = register(ProcessorBlock::new,
            ProcessorType.CENTRIFUGE
        );
        public static final RegistryObject<ProcessorBlock> ENERGIZER = register(ProcessorBlock::new,
            ProcessorType.ENERGIZER
        );
        public static final RegistryObject<ProcessorBlock> ETCHER = register(ProcessorBlock::new, ProcessorType.ETCHER);

        public static final RegistryObject<MaintainerBlock> MAINTAINER = register(MAINTAINER_ID, MaintainerBlock::new);

        static {
            Assembler.init();
        }

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
            Function<? super ProcessorType, ? extends B> constructor, ProcessorType processorType
        ) {
            RegistryObject<B> block = REGISTRY.register(processorType.getId(), () -> constructor.apply(processorType));
            registerBlockItem(processorType.getId(), block);
            return block;
        }

        private static <B extends GenericBlock> void registerBlockItem(String id, RegistryObject<B> block) {
            Items.REGISTRY.register(id, () -> new BlockItem(block.get(), new Properties().tab(TAB)));
        }

        public static final class Assembler {

            public static final RegistryObject<ControllerBlock> CONTROLLER = Blocks.register(CONTROLLER_ID,
                ControllerBlock::new
            );
            public static final RegistryObject<HullBlock> WALL = register(WALL_ID, HULL_TYPE.WALL, HullBlock::new);
            public static final RegistryObject<HullBlock> FRAME = register(FRAME_ID, HULL_TYPE.FRAME, HullBlock::new);
            public static final RegistryObject<CenterBlock> ACCELERATOR = register(ACCELERATOR_ID,
                CENTER_TYPE.ACCELERATOR,
                CenterBlock::new
            );
            public static final RegistryObject<CenterBlock> TIER_1 = register(TIER_1_ID,
                CENTER_TYPE.TIER_1,
                CenterBlock::new
            );
            public static final RegistryObject<CenterBlock> TIER_2 = register(TIER_2_ID,
                CENTER_TYPE.TIER_2,
                CenterBlock::new
            );
            public static final RegistryObject<CenterBlock> TIER_3 = register(TIER_3_ID,
                CENTER_TYPE.TIER_3,
                CenterBlock::new
            );

            private Assembler() {}

            private static <E extends Enum<?>, B extends GenericBlock> RegistryObject<B> register(
                String id, E type, Function<? super E, ? extends B> constructor
            ) {
                RegistryObject<B> block = REGISTRY.register(id, () -> constructor.apply(type));
                registerBlockItem(id, block);
                return block;
            }

            private static void init() {
                // fake init
            }
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
            public static final TagKey<Item> DUSTS_COAL = forge("dusts/coal");
            public static final TagKey<Item> DUSTS_CARBONIC_FLUIX = forge("dusts/carbonic_fluix");
            public static final TagKey<Item> GEMS_RESONATING = forge("gems/resonating");
            public static final TagKey<Item> INGOTS_FLUIX_IRON = forge("ingots/fluix_iron");
            public static final TagKey<Item> INGOTS_FLUIX_STEEL = forge("ingots/fluix_steel");

            public static final TagKey<Item> PROCESSORS = mod("processors");
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

            public static final TagKey<Block> MACHINES = mod("machines");

            private Blocks() {}

            @SuppressWarnings("SameParameterValue")
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

            public static final RegistryObject<RecipeSerializer<ProcessorRecipe>> AGGREGATOR
                = register(ProcessorType.AGGREGATOR);
            public static final RegistryObject<RecipeSerializer<ProcessorRecipe>> CENTRIFUGE
                = register(ProcessorType.CENTRIFUGE);
            public static final RegistryObject<RecipeSerializer<ProcessorRecipe>> ENERGIZER
                = register(ProcessorType.ENERGIZER);
            public static final RegistryObject<RecipeSerializer<ProcessorRecipe>> ETCHER
                = register(ProcessorType.ETCHER);

            private Serializers() {}

            private static RegistryObject<RecipeSerializer<ProcessorRecipe>> register(
                ProcessorType processorType
            ) {
                return REGISTRY.register(processorType.getId(), () -> new ProcessorRecipeSerializer(processorType));
            }
        }
    }
}
