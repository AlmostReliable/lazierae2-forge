package com.almostreliable.lazierae2.forge;

import com.almostreliable.almostlib.AlmostLib;
import com.almostreliable.almostlib.forge.client.ClientInitRunner;
import com.almostreliable.lazierae2.BuildConfig;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.client.ClientSetup;
import com.almostreliable.lazierae2.forge.data.DataGeneration;
import com.almostreliable.lazierae2.registry.LazierMenus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.almostreliable.lazierae2.LazierAE2.MANAGER;

@Mod(BuildConfig.MOD_ID)
public class ForgeInitializer {

    public ForgeInitializer() {
        LazierAE2.init();
        MANAGER.initRegistriesToLoader();
        AlmostLib.PLATFORM.initRegistration(LazierMenus.REGISTRY);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ForgeInitializer::commonSetup);
        modEventBus.addListener(DataGeneration::init);

        ClientInitRunner.run(ClientSetup::new);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        LazierAE2.initPost();
    }
}
