package com.almostreliable.lazierae2;

import appeng.api.config.Upgrades;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Containers;
import com.almostreliable.lazierae2.data.DataGeneration;
import com.almostreliable.lazierae2.gui.MachineScreen;
import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.network.PacketHandler;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
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
        ModLoadingContext context = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        context.registerConfig(Type.COMMON, Config.COMMON_SPEC);
        modEventBus.addListener(LazierAE2::onCommonSetup);
        modEventBus.addListener(LazierAE2::onClientSetup);
        modEventBus.addListener(DataGeneration::init);
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
        // add compatibility to acceleration card for own blocks
        Upgrades.SPEED.registerItem(Blocks.AGGREGATOR.get().asItem(), MachineType.AGGREGATOR.getUpgradeSlots());
        Upgrades.SPEED.registerItem(Blocks.CENTRIFUGE.get().asItem(), MachineType.CENTRIFUGE.getUpgradeSlots());
        Upgrades.SPEED.registerItem(Blocks.ENERGIZER.get().asItem(), MachineType.ENERGIZER.getUpgradeSlots());
        Upgrades.SPEED.registerItem(Blocks.ETCHER.get().asItem(), MachineType.ETCHER.getUpgradeSlots());
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        ScreenManager.register(Containers.MACHINE.get(), MachineScreen::new);
    }
}
