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
                    36,
                    83
                ));
            }
        }
    }

    @Override
    protected int getPlayerInventoryHeight() {
        return 113;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = slots.get(index);

        // check if the slot has an item inside
        if (!slot.hasItem()) return stack;

        var slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index < controllerData.getSlots()) {
            // from controller to inventory
            if (!moveItemStackTo(
                slotStack,
                controllerData.getSlots(),
                controllerData.getSlots() + PLAYER_INV_SIZE,
                false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            // from inventory to controller
            if (!moveItemStackTo(
                slotStack,
                0,
                controllerData.getSlots(),
                false
            )) {
                return ItemStack.EMPTY;
            }
        }

        // check if something changed
        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);

        return stack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (entity.getLevel() == null || entity.getLevel().isClientSide) return;
        controllerData.validateSize();
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
