package com.almostreliable.lazierae2.content.assembler.controller;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.assembler.AssemblerBlock;
import com.almostreliable.lazierae2.core.Setup.Menus.Assembler;
import com.almostreliable.lazierae2.inventory.PatternReferenceSlot;
import com.almostreliable.lazierae2.inventory.PatternSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ControllerMenu extends GenericMenu<ControllerEntity> {

    public static final int ROWS = 4;
    public static final int COLUMNS = 9;
    private static final int SLOT_SIZE = 18;
    public final ControllerData controllerData;

    public ControllerMenu(
        int id, ControllerEntity entity,
        Inventory menuInventory
    ) {
        super(Assembler.CONTROLLER.get(), id, entity, menuInventory);
        controllerData = entity.controllerData;
        setupContainerInventory();
        setupPlayerInventory();
        setupClientSlots();
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && AssemblerBlock.isMultiBlock(entity.getBlockState());
    }

    @Override
    protected void setupContainerInventory() {
        for (var row = 0; row < controllerData.getSlots() / COLUMNS; row++) {
            for (var slot = 0; slot < COLUMNS; slot++) {
                addSlot(new PatternSlot(
                    controllerData,
                    row * COLUMNS + slot,
                    61,
                    83
                ));
            }
        }
    }

    @Override
    protected int getPlayerInventoryHeight() {
        return 118;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void setupClientSlots() {
        if (entity.getLevel() != null && entity.getLevel().isClientSide) {
            for (var row = 0; row < ROWS; row++) {
                for (var slot = 0; slot < COLUMNS; slot++) {
                    var index = row * COLUMNS + slot;
                    if (index >= controllerData.getSlots()) return;
                    addSlot(new PatternReferenceSlot(
                        this,
                        controllerData,
                        index,
                        8 + slot * SLOT_SIZE,
                        8 + row * SLOT_SIZE
                    ));
                }
            }
        }
    }
}
