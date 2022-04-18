package com.almostreliable.lazierae2;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.data.DataGeneration;
import com.almostreliable.lazierae2.gui.MaintainerScreen;
import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.network.PacketHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@Mod(MOD_ID)
@EventBusSubscriber
public class LazierAE2 {

    @SuppressWarnings("java:S1118")
    public LazierAE2() {
        var context = ModLoadingContext.get();
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        context.registerConfig(Type.COMMON, Config.COMMON_SPEC);
        modEventBus.addListener(LazierAE2::onCommonSetup);
        modEventBus.addListener(LazierAE2::onClientSetup);
        modEventBus.addListener(DataGeneration::init);
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
        // add compatibility to acceleration card for own blocks
        Upgrades.add(AEItems.SPEED_CARD.asItem(),
            Blocks.AGGREGATOR.get().asItem(),
            ProcessorType.AGGREGATOR.getUpgradeSlots()
        );
        Upgrades.add(AEItems.SPEED_CARD.asItem(),
            Blocks.CENTRIFUGE.get().asItem(),
            ProcessorType.CENTRIFUGE.getUpgradeSlots()
        );
        Upgrades.add(AEItems.SPEED_CARD.asItem(),
            Blocks.ENERGIZER.get().asItem(),
            ProcessorType.ENERGIZER.getUpgradeSlots()
        );
        Upgrades.add(AEItems.SPEED_CARD.asItem(), Blocks.ETCHER.get().asItem(), ProcessorType.ETCHER.getUpgradeSlots());
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Menus.PROCESSOR.get(), ProcessorScreen::new);
            MenuScreens.register(Menus.MAINTAINER.get(), MaintainerScreen::new);
        });
    }
}
