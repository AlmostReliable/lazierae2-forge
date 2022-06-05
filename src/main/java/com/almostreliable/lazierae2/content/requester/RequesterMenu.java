package com.almostreliable.lazierae2.content.requester;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;
import com.almostreliable.lazierae2.inventory.FakeSlot;
import com.almostreliable.lazierae2.network.sync.handler.EnumDataHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RequesterMenu extends GenericMenu<RequesterEntity> {

    private static final int SLOT_GAP = 2;
    private final RequesterInventory requesterInventory;

    public RequesterMenu(
        int windowId, RequesterEntity entity, Inventory menuInventory
    ) {
        super(Menus.REQUESTER.get(), windowId, entity, menuInventory);
        requesterInventory = entity.craftRequests;
        setupContainerInventory();
        setupPlayerInventory();
        syncData();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        // transfer stack to first empty request slot
        if (slot < requesterInventory.getSlots() || slot > requesterInventory.getSlots() + GenericMenu.PLAYER_INV_SIZE) {
            return ItemStack.EMPTY;
        }
        var stack = getSlot(slot).getItem();
        if (stack.isEmpty()) return ItemStack.EMPTY;
        var targetSlotIndex = requesterInventory.firstAvailableSlot();
        if (targetSlotIndex == -1) return ItemStack.EMPTY;
        var targetSlot = getSlot(targetSlotIndex);
        if (!(targetSlot instanceof FakeSlot fakeSlot) || fakeSlot.isLocked()) return ItemStack.EMPTY;
        fakeSlot.set(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < requesterInventory.getSlots()) {
            var slot = getSlot(slotId);
            if (slot instanceof FakeSlot fakeSlot && !fakeSlot.isLocked()) {
                handleClick(dragType, clickType, slot);
                return;
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    public boolean getRequestState(int slot) {
        return entity.craftRequests.get(slot).getState();
    }

    public ItemStack getRequestStack(int slot) {
        return entity.craftRequests.get(slot).getStack();
    }

    public long getRequestCount(int slot) {
        return entity.craftRequests.get(slot).getCount();
    }

    public long getRequestBatch(int slot) {
        return entity.craftRequests.get(slot).getBatch();
    }

    public PROGRESSION_TYPE getRequestStatus(int slot) {
        return entity.getProgression(slot).type();
    }

    @Override
    protected void setupContainerInventory() {
        for (var i = 0; i < requesterInventory.getSlots(); i++) {
            addSlot(new FakeSlot(this, requesterInventory, i, 26, 7 + (i * SLOT_SIZE) + (i * SLOT_GAP)));
        }
    }

    @Override
    protected int getSlotY() {
        return 129;
    }

    private void syncData() {
        for (var slot = 0; slot < requesterInventory.getSlots(); slot++) {
            var finalSlot = slot;
            sync.addDataHandler(requesterInventory.get(finalSlot));
            sync.addDataHandler(new EnumDataHandler<>(
                () -> getRequestStatus(finalSlot).translateToClient(),
                value -> entity.setClientProgression(finalSlot, value),
                PROGRESSION_TYPE.values()
            ));
        }
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
}
