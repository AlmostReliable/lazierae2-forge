package com.github.almostreliable.lazierae2;

import appeng.api.config.Upgrades;
import com.github.almostreliable.lazierae2.core.Setup;
import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.data.DataGeneration;
import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

@Mod(MOD_ID)
@EventBusSubscriber
public class LazierAE2 {

    @SuppressWarnings("java:S1118")
    public LazierAE2() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // register common setup listener
        modEventBus.addListener(LazierAE2::onCommonSetup);
        // register client listener
        modEventBus.addListener(LazierAE2::onClientSetup);
        // register data gen listener
        modEventBus.addListener(DataGeneration::init);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // initialize packet handler
        PacketHandler.init();
        // add compatibility to acceleration card for own blocks
        // TODO: read max supported amount of upgrades from config
        Upgrades.SPEED.registerItem(Blocks.AGGREGATOR.get().asItem(), 8);
        Upgrades.SPEED.registerItem(Blocks.CENTRIFUGE.get().asItem(), 8);
        Upgrades.SPEED.registerItem(Blocks.ENERGIZER.get().asItem(), 8);
        Upgrades.SPEED.registerItem(Blocks.ETCHER.get().asItem(), 8);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // register screens
        ScreenManager.register(Containers.MACHINE.get(), MachineScreen::new);
    }
}
