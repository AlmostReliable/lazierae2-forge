package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.inventory.OutputSlot;
import com.github.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.github.almostreliable.lazierae2.network.IntReferenceGetter;
import com.github.almostreliable.lazierae2.tile.MachineTile;
import com.github.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class MachineContainer extends Container {

    private static final int PLAYER_INV_SIZE = 36;
    private final MachineTile tile;
    private IItemHandler inventory;

    protected MachineContainer(ContainerType<?> type, int id, MachineTile tile) {
        super(type, id);
        this.tile = tile;
        // set up container inventory if the tile exposes an item handler capability
        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            setupContainerInv(inv);
            setupPlayerInventory(inv);
            inventory = inv;
        });

        syncData();
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        // TODO: refactor this
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        // check if the slot exists and has an item inside
        if (slot == null || !slot.hasItem()) return stack;

        ItemStack slotStack = slot.getItem();
        stack = slotStack.copy();

        // decide where to put the item
        if (index < inventory.getSlots()) {
            // transfer item from machine to inventory
            if (!moveItemStackTo(slotStack, inventory.getSlots(), inventory.getSlots() + PLAYER_INV_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GameUtil.isUpgrade(slotStack)) {
            // transfer item from inventory to upgrade slot
            if (!moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            // transfer item from inventory to machine input slots
            Slot inputSlot = inputsContainItem(stack);
            if (slotStack.isStackable() && inputSlot != null && (inputSlot.getItem().getCount() <
                Math.min(inputSlot.getMaxStackSize(), inputSlot.getItem().getMaxStackSize()))) {
                // merge item stack if one input slot has the same item already
                mergeItemStackTo(stack, inputSlot);
            } else {
                // transfer item stack if there are empty inventory slots
                if (!moveItemStackTo(slotStack, 2, inventory.getSlots(), false)) return ItemStack.EMPTY;
            }
        }

        // check if something changed
        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            // call this so the tile entity is marked as changed and saved
            slot.setChanged();
        }

        if (slotStack.getCount() == stack.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);

        return stack;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return tile.getLevel() != null &&
            Container.stillValid(IWorldPosCallable.create(tile.getLevel(), tile.getBlockPos()),
                player,
                tile.getBlockState().getBlock()
            );
    }

    private void syncData() {
        addDataSlot(new IntReferenceGetter(tile::getProgress));
        addDataSlot(new IntReferenceGetter(tile::getProcessTime));
        // energy lower bits
        addDataSlot(new IntReferenceGetter(() -> getEnergyStored() & 0xFFFF));
        // energy upper bits
        addDataSlot(new IntReferenceGetter(() -> (getEnergyStored() >>> 16) & 0xFFFF));
    }

    /**
     * Adds the container slots to the inventory.
     *
     * @param inventory the inventory to add the slots to
     */
    private void setupContainerInv(IItemHandler inventory) {
        int inputSlots = tile.getInputSlots();

        // upgrade slot
        addSlot(new UpgradeSlot(inventory, 0, 146, 62));
        // output slot
        addSlot(new OutputSlot(inventory, 1, inputSlots == 1 ? 116 : 120, 35));
        // input slot
        if (inputSlots == 1) addSlot(new SlotItemHandler(inventory, 2, 56, 35));
    }

    /**
     * Adds the player inventory slots to the inventory.
     *
     * @param inventory the inventory to add the slots to
     */
    private void setupPlayerInventory(IItemHandler inventory) {
        // main inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new SlotItemHandler(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        // hot bar
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 142));
        }
    }

    /**
     * Checks if the input slots of the inventory contain the given item.
     *
     * @param stack the item to check for
     * @return the slot containing the item, or null if not found
     */
    @Nullable
    private Slot inputsContainItem(ItemStack stack) {
        return IntStream
            .range(InventoryHandler.NON_INPUT_SLOTS, tile.getInputSlots() + InventoryHandler.NON_INPUT_SLOTS)
            .mapToObj(slots::get)
            .filter(slot -> Container.consideredTheSameItem(slot.getItem(), stack))
            .findFirst()
            .orElse(null);
    }

    /**
     * Tries to merge the given item stack to the given slot.
     * <p>
     * If the slot changed data, the slot is marked as changed and the tile entity is saved.
     *
     * @param stack the item stack to merge
     * @param slot  the slot to merge the item stack to
     */
    private void mergeItemStackTo(ItemStack stack, Slot slot) {
        ItemStack slotStack = slot.getItem();

        int mergedAmount = slotStack.getCount() + stack.getCount();
        int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
        if (mergedAmount <= maxSize) {
            stack.setCount(0);
            slotStack.setCount(mergedAmount);
            slot.setChanged();
        } else if (slotStack.getCount() < maxSize) {
            stack.shrink(maxSize - slotStack.getCount());
            slotStack.setCount(maxSize);
            slot.setChanged();
        }
    }

    private int getEnergyStored() {
        return tile.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }
}
