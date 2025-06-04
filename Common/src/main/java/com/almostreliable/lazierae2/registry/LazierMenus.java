package com.almostreliable.lazierae2.registry;

import appeng.core.AppEng;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae2.menu.CrystalInjectorMenu;
import com.almostreliable.lib.registry.MenuRegistration;
import com.almostreliable.lib.registry.Registration;
import com.almostreliable.lib.registry.RegistryEntry;
import com.almostreliable.lib.util.AlmostUtils;
import net.minecraft.world.inventory.MenuType;

public final class LazierMenus {

    public static final MenuRegistration REGISTRY = Registration.menus(AppEng.MOD_ID);

    //@formatter:off

    // machines
    public static final RegistryEntry<MenuType<CrystalInjectorMenu>> INJECTOR = register("crystal_injector", MenuTypeBuilder.create(CrystalInjectorMenu::new, CrystalInjectorBlockEntity.class));

    //@formatter:on

    private LazierMenus() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing menus");
    }

    private static <M extends AEBaseMenu> RegistryEntry<MenuType<M>> register(
        String id, MenuTypeBuilder<M, ?> menuTypeBuilder
    ) {
        RegistryEntry<MenuType<M>> entry = REGISTRY.register(id, () -> menuTypeBuilder.build(id));
        LazierAE2.MANAGER.dataGen()
            .common()
            .lang(p -> p.addLang("gui.lazierae2." + id, AlmostUtils.capitalizeWords(id.replace("_", " "))));
        return entry;
    }
}
