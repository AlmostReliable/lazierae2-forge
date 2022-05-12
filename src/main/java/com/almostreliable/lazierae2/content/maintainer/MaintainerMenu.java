package com.almostreliable.lazierae2.content.maintainer;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;
import com.almostreliable.lazierae2.inventory.FakeSlot;
import com.almostreliable.lazierae2.progression.ClientState;
import com.almostreliable.lazierae2.util.DataSlotUtil;
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
        requestInventory = entity.getCraftRequests();
        setupContainerInventory();
        setupPlayerInventory();
        syncData();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        // transfer stack to first empty request slot
        if (slot < requestInventory.getSlots() || slot > requestInventory.getSlots() + GenericMenu.PLAYER_INV_SIZE) {
            return ItemStack.EMPTY;
        }
        var stack = getSlot(slot).getItem();
        if (stack.isEmpty()) return ItemStack.EMPTY;
        var targetSlotIndex = requestInventory.firstAvailableSlot();
        if (targetSlotIndex == -1) return ItemStack.EMPTY;
        var targetSlot = getSlot(targetSlotIndex);
        if (!(targetSlot instanceof FakeSlot fakeSlot) || fakeSlot.isLocked()) return ItemStack.EMPTY;
        fakeSlot.set(stack);
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < requestInventory.getSlots()) {
            var slot = getSlot(slotId);
            if (slot instanceof FakeSlot fakeSlot && !fakeSlot.isLocked()) {
                handleClick(dragType, clickType, slot);
                return;
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    public boolean getRequestState(int slot) {
        return entity.getCraftRequests().get(slot).state();
    }

    public long getRequestCount(int slot) {
        return entity.getCraftRequests().get(slot).count();
    }

    public long getRequestBatch(int slot) {
        return entity.getCraftRequests().get(slot).batch();
    }

    public PROGRESSION_TYPE getProgressionType(int slot) {
        if (!(entity.getProgressions(slot) instanceof ClientState)) {
            throw new IllegalStateException("Progression " + slot + " is not a ClientState");
        }
        var type = entity.getProgressions(slot).type();
        if (type == PROGRESSION_TYPE.REQUEST || type == PROGRESSION_TYPE.PLAN) return PROGRESSION_TYPE.IDLE;
        return type;
    }

    @Override
    protected void setupContainerInventory() {
        for (var i = 0; i < requestInventory.getSlots(); i++) {
            addSlot(new FakeSlot(this, requestInventory, i, 26, 7 + (i * SLOT_SIZE) + (i * SLOT_GAP)));
        }
    }

    @Override
    protected int getSlotY() {
        return 129;
    }

    private void syncData() {
        // current progression type for all slots
        for (var slot = 0; slot < requestInventory.getSlots(); slot++) {
            var finalSlot = slot;
            addDataSlot(DataSlotUtil.forInteger(
                entity,
                () -> entity.getProgressions(finalSlot).type().ordinal(),
                value -> entity.setClientProgression(finalSlot, PROGRESSION_TYPE.values()[value])
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

    public int getRequestSlots() {
        return entity.getCraftRequests().getSlots();
    }
}
