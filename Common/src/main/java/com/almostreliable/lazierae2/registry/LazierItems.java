package com.almostreliable.lazierae2.registry;

import appeng.items.materials.MaterialItem;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lib.registry.ItemEntry;
import com.almostreliable.lib.registry.ItemRegistration;

public final class LazierItems {

    private static final ItemRegistration REGISTRY = LazierAE2.MANAGER.items();

    //@formatter:off

    // fluix materials
    public static final ItemEntry<MaterialItem> CARB_FLUIX_DUST = REGISTRY.simple("carbonic_fluix_dust", MaterialItem::new);
    public static final ItemEntry<MaterialItem> FLUIX_STEEL = REGISTRY.simple("fluix_steel_ingot", MaterialItem::new);

    // resonating materials
    public static final ItemEntry<MaterialItem> RESONATING_DUST = REGISTRY.simple("resonating_dust", MaterialItem::new);
    public static final ItemEntry<MaterialItem> RESONATING_CRYSTAL = REGISTRY.simple("resonating_crystal", MaterialItem::new);

    // misc materials
    public static final ItemEntry<MaterialItem> COAL_DUST = REGISTRY.simple("coal_dust", MaterialItem::new);

    // // crafting items
    public static final ItemEntry<MaterialItem> LOGIC_UNIT = REGISTRY.simple("logic_unit", MaterialItem::new);
    public static final ItemEntry<MaterialItem> GROWTH_CORE = REGISTRY.simple("growth_core", MaterialItem::new);

    // presses, prints and processors
    public static final ItemEntry<MaterialItem> UNIVERSAL_PRESS = REGISTRY.simple("universal_press", MaterialItem::new);
    public static final ItemEntry<MaterialItem> FLUIX_PRINTED = REGISTRY.simple("fluix_printed", "Printed Fluix Circuit", MaterialItem::new);
    public static final ItemEntry<MaterialItem> FLUIX_PROCESSOR = REGISTRY.simple("fluix_processor", MaterialItem::new);
    public static final ItemEntry<MaterialItem> RESONATING_PRINTED = REGISTRY.simple("resonating_printed", "Printed Resonating Circuit", MaterialItem::new);
    public static final ItemEntry<MaterialItem> RESONATING_PROCESSOR = REGISTRY.simple("resonating_processor", MaterialItem::new);

    //@formatter:on

    private LazierItems() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing items");
    }
}
