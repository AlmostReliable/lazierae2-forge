package com.almostreliable.lazierae.registry;

import net.minecraft.world.item.ItemStack;

import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lib.item.AlmostCreativeTab;

public final class LazierRegistration {

    private LazierRegistration() {}

    public static void init() {
        LazierAE2.LOGGER.info("Initializing content");

        LazierAE2.MANAGER.defaultCreativeTab(new AlmostCreativeTab(LazierAE2.MANAGER.getRL("tab"), LazierAE2Constants.MOD_NAME) {
            @Override
            public ItemStack makeIcon() {
                return LazierItems.RESONATING_CRYSTAL.asStack();
            }
        });

        LazierBlocks.init();
        LazierItems.init();
        LazierBlockEntities.init();
        LazierMenus.init();
        LazierRecipes.init();
    }
}
