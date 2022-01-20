package com.github.almostreliable.lazierae2;

import com.github.almostreliable.lazierae2.core.Setup;
import com.github.almostreliable.lazierae2.data.DataGeneration;
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
