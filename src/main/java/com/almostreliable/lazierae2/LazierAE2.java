package com.almostreliable.lazierae2;

import com.almostreliable.lazierae2.core.LazierConfig;
import com.almostreliable.lazierae2.core.LazierRegistration;
import com.almostreliable.lazierae2.data.DataGeneration;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(BuildConfig.MOD_ID)
public final class LazierAE2 {

    public static final Logger LOGGER = LogUtils.getLogger();

    public LazierAE2(IEventBus modEventBus) {
        LazierRegistration.init(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, LazierConfig.COMMON_SPEC);
        modEventBus.addListener(DataGeneration::init);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(BuildConfig.MOD_ID, path);
    }
}
