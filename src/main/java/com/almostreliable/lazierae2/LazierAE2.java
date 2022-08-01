package com.almostreliable.lazierae2;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.items.misc.CrystalSeedItem;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Items;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.core.Setup.Menus.Assembler;
import com.almostreliable.lazierae2.data.DataGeneration;
import com.almostreliable.lazierae2.gui.ControllerScreen;
import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.gui.RequesterScreen;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static appeng.init.client.InitItemModelsProperties.GROWTH_PREDICATE_ID;
import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("UtilityClassWithPublicConstructor")
@Mod(MOD_ID)
@EventBusSubscriber
public final class LazierAE2 {

    public static final Logger LOG = LogUtils.getLogger();

    @SuppressWarnings("java:S1118")
    public LazierAE2() {
        var context = ModLoadingContext.get();
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        context.registerConfig(Type.COMMON, Config.COMMON_SPEC);
        modEventBus.addListener(LazierAE2::onCommonSetup);
        modEventBus.addListener(LazierAE2::onClientSetup);
        modEventBus.addListener(Config::onConfigReloaded);
        modEventBus.addListener(DataGeneration::init);
        Setup.init(modEventBus);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        PacketHandler.init();
        // add compatibility to acceleration card for own blocks
        Upgrades.add(
            AEItems.SPEED_CARD.asItem(),
            Blocks.AGGREGATOR.get().asItem(),
            ProcessorType.AGGREGATOR.getUpgradeSlots()
        );
        Upgrades.add(AEItems.SPEED_CARD.asItem(), Blocks.ETCHER.get().asItem(), ProcessorType.ETCHER.getUpgradeSlots());
        Upgrades.add(
            AEItems.SPEED_CARD.asItem(),
            Blocks.GRINDER.get().asItem(),
            ProcessorType.GRINDER.getUpgradeSlots()
        );
        Upgrades.add(
            AEItems.SPEED_CARD.asItem(),
            Blocks.INFUSER.get().asItem(),
            ProcessorType.INFUSER.getUpgradeSlots()
        );
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Menus.PROCESSOR.get(), ProcessorScreen::new);
            MenuScreens.register(Menus.REQUESTER.get(), RequesterScreen::new);
            MenuScreens.register(Assembler.CONTROLLER.get(), ControllerScreen::new);
            ItemProperties.register(
                Items.RESONATING_SEED.get(),
                GROWTH_PREDICATE_ID,
                (is, level, p, s) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED
            );
        });
    }
}
