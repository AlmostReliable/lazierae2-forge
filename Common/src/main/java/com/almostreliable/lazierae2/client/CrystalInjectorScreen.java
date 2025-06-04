package com.almostreliable.lazierae2.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import com.almostreliable.lazierae2.menu.CrystalInjectorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalInjectorScreen extends UpgradeableScreen<CrystalInjectorMenu> {
    public CrystalInjectorScreen(CrystalInjectorMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }
}
