package com.almostreliable.lazierae;

import com.almostreliable.lazierae.client.LazierClient;
import com.almostreliable.lazierae.data.DataGeneration;
import com.almostreliable.lazierae.registry.LazierRegistration;
import com.almostreliable.lib.AlmostManager;
import com.almostreliable.lib.config.ConfigHolder;
import com.almostreliable.lib.config.ConfigManager;
import com.almostreliable.lib.forge.client.ClientInitRunner;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("WeakerAccess")
@Mod(LazierAE2Constants.MOD_ID)
public final class LazierAE2 {

    static {
        ConfigManager.registerServerReloadable(LazierAE2Constants.MOD_ID, Config.class, Config::new);
    }

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final AlmostManager MANAGER = AlmostManager.create(LazierAE2Constants.MOD_ID);
    public static final ConfigHolder<Config> CONFIG = ConfigManager.get(Config.class);

    public LazierAE2() {
        onInitialize();
        ClientInitRunner.run(new LazierClient());
    }

    public void onInitialize() {
        LOGGER.info("Applied Energistics 2 is getting lazier now");
        LazierRegistration.init();
        MANAGER.initRegistriesToLoader();

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(DataGeneration::init);
    }
}
