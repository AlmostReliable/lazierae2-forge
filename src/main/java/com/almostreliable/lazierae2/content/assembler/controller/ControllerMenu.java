package com.almostreliable.lazierae2.content.assembler.controller;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus.Assembler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ControllerMenu extends GenericMenu<ControllerEntity> {

    private static final int SLOT_SIZE = 18;

    public ControllerMenu(
        int id, ControllerEntity entity,
        Inventory menuInventory
    ) {
        super(Assembler.CONTROLLER.get(), id, entity, menuInventory);
        setupContainerInventory();
        setupPlayerInventory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void setupContainerInventory() {
        // TODO: Implement
    }

    @Override
    protected int getPlayerInventoryHeight() {
        return 76;
    }
}
