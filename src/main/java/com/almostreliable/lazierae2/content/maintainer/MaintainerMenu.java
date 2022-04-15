package com.almostreliable.lazierae2.content.maintainer;

import com.almostreliable.lazierae2.component.InventoryHandler.RequestInventory;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.inventory.FakeSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MaintainerMenu extends GenericMenu<MaintainerEntity> {

    private static final int SLOT_GAP = 2;
    private final RequestInventory requestInventory;

    public MaintainerMenu(
        int windowId, MaintainerEntity entity, Inventory menuInventory
    ) {
        super(Menus.MAINTAINER.get(), windowId, entity, menuInventory);
        requestInventory = entity.craftRequests;
        setupContainerInventory();
        setupPlayerInventory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < requestInventory.getSlots()) {
            var slot = getSlot(slotId);
            if (slot instanceof FakeSlot) {
                handleClick(dragType, clickType, slot);
                return;
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    public boolean getRequestState(int slot) {
        return entity.craftRequests.getState(slot);
    }

    public long getRequestCount(int slot) {
        return entity.craftRequests.getCount(slot);
    }

    public long getRequestBatch(int slot) {
        return entity.craftRequests.getBatch(slot);
    }

    @Override
    protected void setupContainerInventory() {
        for (var i = 0; i < requestInventory.getSlots(); i++) {
            addSlot(new FakeSlot(requestInventory, i, 26, 7 + (i * SLOT_SIZE) + (i * SLOT_GAP)));
        }
    }

    @Override
    protected int getSlotY() {
        return 129;
    }

    private void handleClick(int dragType, ClickType clickType, Slot slot) {
        var hand = getCarried();
        if (clickType == ClickType.PICKUP) {
            if (hand.isEmpty()) {
                slot.set(ItemStack.EMPTY);
                return;
            }
            if (dragType == 0) {
                // left click
                slot.set(hand.copy());
            } else {
                // right click
                var stack = hand.copy();
                stack.setCount(1);
                slot.set(stack);
            }
        } else if (clickType == ClickType.QUICK_MOVE) {
            slot.set(ItemStack.EMPTY);
        }
    }

    public int getRequestSlots() {
        return entity.craftRequests.getSlots();
    }
}
