package com.almostreliable.lazierae2.menu;

import appeng.menu.implementations.UpgradeableMenu;
import com.almostreliable.lazierae2.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae2.registry.LazierMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CrystalInjectorMenu extends UpgradeableMenu<CrystalInjectorBlockEntity> {

    public CrystalInjectorMenu(int wid, Inventory inventory, CrystalInjectorBlockEntity blockEntity) {
        super(LazierMenus.INJECTOR.get(), wid, inventory, blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
