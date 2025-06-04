package com.almostreliable.lazierae.registry;

import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae.menu.CrystalInjectorMenu;
import com.almostreliable.lib.registry.MenuEntry;
import com.almostreliable.lib.registry.MenuRegistration;

public final class LazierMenus {

    private static final MenuRegistration REGISTRY = LazierAE2.MANAGER.menus();

    //@formatter:off

    // machines
    public static final MenuEntry<CrystalInjectorMenu> INJECTOR = REGISTRY.forBlockEntity("crystal_injector", CrystalInjectorBlockEntity.class, CrystalInjectorMenu::new);

    //@formatter:on

    private LazierMenus() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing menus");
    }
}
