package com.almostreliable.lazierae2.content.assembler.controller;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus.Assembler;
import com.almostreliable.lazierae2.inventory.PatternSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ControllerMenu extends GenericMenu<ControllerEntity> {

    private static final int SLOT_SIZE = 18;
    private final ControllerData controllerData;

    public ControllerMenu(
        int id, ControllerEntity entity,
        Inventory menuInventory
    ) {
        super(Assembler.CONTROLLER.get(), id, entity, menuInventory);
        controllerData = entity.controllerData;
        setupContainerInventory();
        setupPlayerInventory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void setupContainerInventory() {
        for (var row = 0; row < controllerData.getSlots() / 9; row++) {
            for (var slot = 0; slot < 9; slot++) {
                addSlot(new PatternSlot(
                    this,
                    controllerData,
                    slot + row * 9,
                    8 + slot * SLOT_SIZE,
                    11 + row * SLOT_SIZE
                ));
            }
        }
    }

    @Override
    protected int getPlayerInventoryHeight() {
        return 76;
    }
}
