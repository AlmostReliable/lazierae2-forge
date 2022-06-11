package com.almostreliable.lazierae2.content.assembler.holder;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus.Assembler;
import com.almostreliable.lazierae2.inventory.PatternSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PatternHolderMenu extends GenericMenu<PatternHolderEntity> {

    private static final int SLOT_SIZE = 18;
    private final PatternInventory patternStorage;

    public PatternHolderMenu(
        int id, PatternHolderEntity entity,
        Inventory menuInventory
    ) {
        super(Assembler.PATTERN_HOLDER.get(), id, entity, menuInventory);
        patternStorage = entity.patternStorage;
        setupContainerInventory();
        setupPlayerInventory();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = slots.get(index);

        if (!slot.hasItem()) return stack;

        var slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index < patternStorage.getSlots()) {
            // from pattern holder to inventory
            if (!moveItemStackTo(
                slotStack,
                patternStorage.getSlots(),
                patternStorage.getSlots() + PLAYER_INV_SIZE,
                false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            // from inventory to pattern holder
            if (!moveItemStackTo(
                slotStack,
                0,
                patternStorage.getSlots(),
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
    protected void setupContainerInventory() {
        for (var row = 0; row < entity.getTier().ordinal(); row++) {
            for (var slot = 0; slot < 9; slot++) {
                addSlot(new PatternSlot(
                    this,
                    patternStorage,
                    slot + row * 9,
                    8 + slot * SLOT_SIZE,
                    11 + row * SLOT_SIZE
                ));
            }
        }
    }

    @Override
    protected int getSlotY() {
        return 76;
    }
}
