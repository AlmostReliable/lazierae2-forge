package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.BuildConfig;
import com.almostreliable.lazierae2.Utils;
import com.almostreliable.lazierae2.data.LazierLang;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class LazierItems {

    static final DeferredItemRegister REGISTRY = new DeferredItemRegister();

    // @formatter:off

    // fluix materials
    public static final DeferredItem<Item> CARBONIC_FLUIX_DUST = REGISTRY.register("carbonic_fluix_dust");
    public static final DeferredItem<Item> FLUIX_STEEL_INGOT = REGISTRY.register("fluix_steel_ingot");

    // resonating materials
    public static final DeferredItem<Item> RESONATING_DUST = REGISTRY.register("resonating_dust");
    public static final DeferredItem<Item> RESONATING_CRYSTAL = REGISTRY.register("resonating_crystal");

    // crafting items
    public static final DeferredItem<Item> LOGIC_UNIT = REGISTRY.register("logic_unit");
    public static final DeferredItem<Item> GROWTH_CORE = REGISTRY.register("growth_core");

    // presses, prints and processors
    public static final DeferredItem<Item> UNIVERSAL_PRESS = REGISTRY.register("universal_press");
    public static final DeferredItem<Item> PRINTED_FLUIX_CIRCUIT = REGISTRY.register("printed_fluix_circuit");
    public static final DeferredItem<Item> PRINTED_RESONATING_CIRCUIT = REGISTRY.register("printed_resonating_circuit");
    public static final DeferredItem<Item> FLUIX_PROCESSOR = REGISTRY.register("fluix_processor");
    public static final DeferredItem<Item> RESONATING_PROCESSOR = REGISTRY.register("resonating_processor");

    // @formatter:on

    private LazierItems() {}

    static final class DeferredItemRegister {

        private final DeferredRegister.Items registry = DeferredRegister.createItems(BuildConfig.MOD_ID);

        private DeferredItemRegister() {}

        DeferredItem<Item> register(String id, Supplier<Item> itemSupplier, @Nullable String name) {
            DeferredItem<Item> deferredItem = registry.register(id, itemSupplier);
            LazierLang.LangEntry.of("item", id, Utils.getNameOrFormatId(id, name));
            LazierTab.add(deferredItem);
            return deferredItem;
        }

        private DeferredItem<Item> register(String id) {
            return register(id, () -> new Item(new Item.Properties()), null);
        }

        void register(IEventBus modEventBus) {
            registry.register(modEventBus);
        }
    }
}
