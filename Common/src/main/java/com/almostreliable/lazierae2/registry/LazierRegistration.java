package com.almostreliable.lazierae2.registry;

import com.almostreliable.lazierae2.BuildConfig;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lib.item.AlmostCreativeTab;
import net.minecraft.world.item.ItemStack;

public final class LazierRegistration {

    private LazierRegistration() {}

    public static void init() {
        LazierAE2.LOGGER.info("Initializing creative tab");

        LazierAE2.MANAGER.defaultCreativeTab(
            new AlmostCreativeTab(LazierAE2.MANAGER.getRL("tab"), BuildConfig.MOD_NAME) {
                @Override
                public ItemStack makeIcon() {
                    return LazierItems.RESONATING_CRYSTAL.asStack();
                }
            }
        );

        LazierBlocks.init();
        LazierItems.init();
        LazierBlockEntities.init();
        LazierMenus.init();
    }
}
