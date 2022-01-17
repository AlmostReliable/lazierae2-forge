package com.github.almostreliable.templatemod.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.Container;
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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.almostreliable.templatemod.core.Constants.MOD_ID;

public enum Setup {
    ;

    private static final Tab TAB = new Tab(MOD_ID);

    public static void init(IEventBus modEventBus) {
        Blocks.REGISTRY.register(modEventBus);
        Items.REGISTRY.register(modEventBus);
        Tiles.REGISTRY.register(modEventBus);
        Containers.REGISTRY.register(modEventBus);
    }

    public enum Tiles {
        ;

        private static final DeferredRegister<TileEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

        private static <T extends TileEntity, B extends Block> RegistryObject<TileEntityType<T>> register(
            String id, RegistryObject<B> block, Function<? super BlockState, T> constructor
        ) {
            //noinspection ConstantConditions
            return REGISTRY.register(
                id,
                () -> Builder.of(() -> constructor.apply(block.get().defaultBlockState()), block.get()).build(null)
            );
        }
    }

    public enum Containers {
        ;

        private static final DeferredRegister<ContainerType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);

        private static <C extends Container> RegistryObject<ContainerType<C>> register(
            String id, BiFunction<? super TileEntity, ? super Integer, ? extends C> constructor
        ) {
            return REGISTRY.register(id, () -> IForgeContainerType.create((containerID, inventory, data) -> {
                TileEntity tile = inventory.player.level.getBlockEntity(data.readBlockPos());
                return constructor.apply(tile, containerID);
            }));
        }
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

    private static class Blocks {

        private static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

        private static RegistryObject<Block> register(String id, Supplier<? extends Block> supplier) {
            RegistryObject<Block> result = REGISTRY.register(id, supplier);
            Items.REGISTRY.register(id, () -> new BlockItem(result.get(), new Properties().tab(TAB)));
            return result;
        }
    }

    private static class Items {

        private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

        private static RegistryObject<Item> register(String id) {
            return REGISTRY.register(id, () -> new Item(new Properties().tab(TAB)));
        }
    }
}
