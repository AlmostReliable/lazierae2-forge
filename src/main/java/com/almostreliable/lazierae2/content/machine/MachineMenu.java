package com.almostreliable.lazierae2.content.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler.MachineInventory;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.inventory.OutputSlot;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.util.DataSlotUtil;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class MachineMenu extends GenericMenu<MachineEntity> {

    private MachineInventory machineInventory;

    public MachineMenu(int id, MachineEntity entity, Inventory menuInventory) {
        super(Menus.MACHINE.get(), id, entity, menuInventory);
        entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            machineInventory = (MachineInventory) inv;
            setupContainerInventory();
        });
        setupPlayerInventory();
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
        if (index < machineInventory.getSlots()) {
            // transfer item from machine to inventory
            if (!moveItemStackTo(slotStack,
                machineInventory.getSlots(),
                machineInventory.getSlots() + PLAYER_INV_SIZE,
                false
            )) {
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
                if (!moveItemStackTo(slotStack, 2, machineInventory.getSlots(), false)) return ItemStack.EMPTY;
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

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    @Override
    protected void setupContainerInventory() {
        var inputSlots = machineInventory.getInputSlots();
        addSlot(new UpgradeSlot(this, machineInventory, MachineInventory.UPGRADE_SLOT, 8, 50));
        addSlot(new OutputSlot(machineInventory, MachineInventory.OUTPUT_SLOT, 116, 29));
        if (inputSlots == 1) {
            addSlot(new SlotItemHandler(machineInventory, 2, 44, 29));
        } else if (inputSlots == 3) {
            addSlot(new SlotItemHandler(machineInventory, 2, 44, 8));
            addSlot(new SlotItemHandler(machineInventory, 3, 44, 29));
            addSlot(new SlotItemHandler(machineInventory, 4, 44, 50));
        } else {
            throw new IllegalArgumentException(f("Invalid input slot count: {}", inputSlots));
        }
    }

    @Override
    protected int getSlotY() {
        return 72;
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

    /**
     * Checks if the input slots of the inventory contain the given item.
     *
     * @param stack the item to check for
     * @return the slot containing the item, or null if not found
     */
    @Nullable
    private Slot inputsContainItem(ItemStack stack) {
        return IntStream
            .range(MachineInventory.NON_INPUT_SLOTS,
                machineInventory.getInputSlots() + MachineInventory.NON_INPUT_SLOTS
            )
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
        return machineInventory.getUpgradeCount();
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
