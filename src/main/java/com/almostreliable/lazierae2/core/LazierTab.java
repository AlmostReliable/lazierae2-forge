package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.BuildConfig;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.Utils;
import com.almostreliable.lazierae2.data.LazierLang;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public final class LazierTab {

    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, LazierAE2.id("main"));
    private static final List<DeferredItem<Item>> ITEMS = new ArrayList<>();

    private LazierTab() {}

    static void register(RegisterEvent registerEvent) {
        CreativeModeTab tab = CreativeModeTab.builder()
            .title(Utils.translate("tab", "main"))
            .icon(Items.ACACIA_BOAT::getDefaultInstance)
            .build();

        registerEvent.register(Registries.CREATIVE_MODE_TAB, KEY.location(), () -> tab);
        LazierLang.LangEntry.of("tab", "main", BuildConfig.MOD_NAME);
    }

    static void add(DeferredItem<Item> item) {
        ITEMS.add(item);
    }

    static void registerContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != KEY) return;
        for (var item : ITEMS) {
            event.accept(item);
        }
    }
}
