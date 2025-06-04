package com.almostreliable.lazierae2;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.registry.LazierBlocks;
import com.almostreliable.lazierae2.registry.LazierRegistration;
import com.almostreliable.lib.AlmostManager;
import com.almostreliable.lib.BuildConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LazierAE2 {

    public static final Logger LOGGER = LogManager.getLogger(BuildConfig.MOD_NAME);
    public static final AlmostManager MANAGER = AlmostManager.create(BuildConfig.MOD_ID);

    private LazierAE2() {}

    public static void init() {
        LOGGER.info("Applied Energistics 2 is getting lazier now!");
        LazierRegistration.init();
    }

    public static void initPost() {
        Upgrades.add(AEItems.SPEED_CARD, LazierBlocks.INJECTOR, 5);
    }
}
