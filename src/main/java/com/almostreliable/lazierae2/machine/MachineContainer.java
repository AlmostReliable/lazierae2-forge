package com.almostreliable.lazierae2.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.core.Setup.Containers;
import com.almostreliable.lazierae2.inventory.OutputSlot;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.util.DataSlotUtil;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class MachineContainer extends AbstractContainerMenu {

    private static final int PLAYER_INV_SIZE = 36;
    public final MachineEntity entity;
    private InventoryHandler inventory;

    public MachineContainer(int id, MachineEntity entity, Inventory inventory) {
        super(Containers.MACHINE.get(), id);
        this.entity = entity;
        entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            this.inventory = (InventoryHandler) inv;
            setupContainerInv();
        });
        setupPlayerInventory(new InvWrapper(inventory));
        syncData();
    }

    @SuppressWarnings("java:S3776")
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // TODO: this should be refactored, but I'm too lazy to do that for 1.16
        var stack = ItemStack.EMPTY;
        var slot = slots.get(index);

        // check if the slot exists and has an item inside
        if (!slot.hasItem()) return stack;

        var slotStack = slot.getItem();
        stack = slotStack.copy();

        // decide where to put the item
        if (index < inventory.getSlots()) {
            // transfer item from machine to inventory
            if (!moveItemStackTo(slotStack, inventory.getSlots(), inventory.getSlots() + PLAYER_INV_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GameUtil.isValidUpgrade(slotStack)) {
            // transfer item from inventory to upgrade slot
            if (!moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            // transfer item from inventory to machine input slots
            var inputSlot = inputsContainItem(stack);
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
    public boolean stillValid(Player player) {
        return entity.getLevel() != null && AbstractContainerMenu.stillValid(
            ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos()),
            player,
            entity.getBlockState().getBlock()
        );
    }

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    private void syncData() {
        addDataSlot(DataSlotUtil.forBoolean(entity, entity::isAutoExtracting, entity::setAutoExtract));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getProgress, entity::setProgress));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getProcessTime, entity::setProcessTime));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getRecipeTime, entity::setRecipeTime));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getEnergyCost, entity::setEnergyCost));
        addDataSlot(DataSlotUtil.forInteger(entity, entity::getRecipeEnergy, entity::setRecipeEnergy));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(entity, this::getEnergyStored, this::setEnergyStored));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(entity, this::getEnergyCapacity, this::setEnergyCapacity));
        addDataSlots(entity.sideConfig.toContainerData());
    }

    private void addMultipleDataSlots(DataSlot... holders) {
        for (var holder : holders) {
            addDataSlot(holder);
        }
    }

    private void setupContainerInv() {
        var inputSlots = inventory.getInputSlots();
        addSlot(new UpgradeSlot(this, inventory, InventoryHandler.UPGRADE_SLOT, 8, 50));
        addSlot(new OutputSlot(inventory, InventoryHandler.OUTPUT_SLOT, 116, 29));
        if (inputSlots == 1) {
            addSlot(new SlotItemHandler(inventory, 2, 44, 29));
        } else if (inputSlots == 3) {
            addSlot(new SlotItemHandler(inventory, 2, 44, 8));
            addSlot(new SlotItemHandler(inventory, 3, 44, 29));
            addSlot(new SlotItemHandler(inventory, 4, 44, 50));
        } else {
            throw new IllegalArgumentException(f("Invalid input slot count: {}", inputSlots));
        }
    }

    private void setupPlayerInventory(IItemHandler inventory) {
        // main inventory
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                addSlot(new SlotItemHandler(inventory, j + i * 9 + 9, 8 + j * 18, 72 + i * 18));
            }
        }
        // hot bar
        for (var i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 130));
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
            .range(InventoryHandler.NON_INPUT_SLOTS, inventory.getInputSlots() + InventoryHandler.NON_INPUT_SLOTS)
            .mapToObj(slots::get)
            .filter(slot -> ItemStack.isSameItemSameTags(slot.getItem(), stack))
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
        var slotStack = slot.getItem();

        var mergedAmount = slotStack.getCount() + stack.getCount();
        var maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
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

    public int getUpgradeCount() {
        return inventory.getUpgradeCount();
    }

    public int getEnergyStored() {
        return getEnergyCap().map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public void setEnergyStored(int energy) {
        getEnergyCap().ifPresent(energyCap -> ((EnergyHandler) energyCap).setEnergy(energy));
    }

    public int getEnergyCapacity() {
        return getEnergyCap().map(IEnergyStorage::getMaxEnergyStored).orElse(1);
    }

    private void setEnergyCapacity(int capacity) {
        getEnergyCap().ifPresent(energyCap -> ((EnergyHandler) energyCap).setCapacity(capacity));
    }

    private LazyOptional<IEnergyStorage> getEnergyCap() {
        return entity.getCapability(CapabilityEnergy.ENERGY);
    }
}
