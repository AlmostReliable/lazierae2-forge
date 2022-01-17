package com.github.almostreliable.templatemod;

import com.github.almostreliable.templatemod.core.Setup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.github.almostreliable.templatemod.core.Constants.MOD_ID;

@Mod(MOD_ID)
@EventBusSubscriber
public class TemplateMod {

    @SuppressWarnings("java:S1118")
    public TemplateMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // register common setup listener
        modEventBus.addListener(TemplateMod::onCommonSetup);
        // register client listener
        modEventBus.addListener(TemplateMod::onClientSetup);
        // register mod contents
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // initialize packet handler
        // PacketHandler.init();
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // register screens
            // ScreenManager.register(Containers.SOME_CONTAINER.get(), SomeScreen::new);
            // register renderers
            // ClientRegistry.bindTileEntityRenderer(Tiles.SOME_TILE.get(), SomeRenderer::new);
        });
    }
}
