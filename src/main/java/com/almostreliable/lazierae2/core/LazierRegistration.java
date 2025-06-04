package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.network.PacketHandler;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class LazierRegistration {

    private LazierRegistration() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(LazierRegistration::onRegisterEvent);
        modEventBus.addListener(LazierRegistration::registerCapabilities);
        modEventBus.addListener(LazierTab::registerContents);
        modEventBus.addListener(PacketHandler::registerPackets);

        LazierBlocks.REGISTRY.register(modEventBus);
        LazierItems.REGISTRY.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(LazierClientRegistration::registerScreens);
        }
    }

    private static void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
            LazierTab.register(event);
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
    }

    private static class LazierClientRegistration {

        private static void registerScreens(RegisterMenuScreensEvent event) {
        }
    }
}
