package com.almostreliable.lazierae2.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.core.Setup.Containers;
import com.almostreliable.lazierae2.inventory.OutputSlot;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.util.DataSlotUtil;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class MachineContainer extends Container {

    private static final int PLAYER_INV_SIZE = 36;
    public final MachineTile tile;
    private InventoryHandler inventory;

    public MachineContainer(int id, MachineTile tile, PlayerInventory playerInventory) {
        super(Containers.MACHINE.get(), id);
        this.tile = tile;
        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            inventory = (InventoryHandler) inv;
            setupContainerInv();
        });
        setupPlayerInventory(new InvWrapper(playerInventory));
        syncData();
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        // check if the slot has an item inside
        if (!slot.hasItem()) return stack;

        ItemStack slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index < inventory.getSlots()) {
            // from machine to inventory
            if (!moveItemStackTo(slotStack, inventory.getSlots(), inventory.getSlots() + PLAYER_INV_SIZE, false)) {
                return ItemStack.EMPTY;
            }
        } else if (GameUtil.isValidUpgrade(slotStack)) {
            // from inventory to upgrade slot
            slotStack = inventory.insertWithinRange(slotStack,
                InventoryHandler.UPGRADE_SLOT,
                InventoryHandler.UPGRADE_SLOT + 1
            );
        } else {
            // from inventory to machine inputs
            slotStack = inventory.insertWithinRange(slotStack,
                InventoryHandler.NON_INPUT_SLOTS,
                InventoryHandler.NON_INPUT_SLOTS + inventory.getInputSlots()
            );
        }

        // check if something changed
        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.set(slotStack);
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

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    private void syncData() {
        addDataSlot(DataSlotUtil.forBoolean(tile, tile::isAutoExtracting, tile::setAutoExtract));
        addDataSlot(DataSlotUtil.forInteger(tile, tile::getProgress, tile::setProgress));
        addDataSlot(DataSlotUtil.forInteger(tile, tile::getProcessTime, tile::setProcessTime));
        addDataSlot(DataSlotUtil.forInteger(tile, tile::getRecipeTime, tile::setRecipeTime));
        addDataSlot(DataSlotUtil.forInteger(tile, tile::getEnergyCost, tile::setEnergyCost));
        addDataSlot(DataSlotUtil.forInteger(tile, tile::getRecipeEnergy, tile::setRecipeEnergy));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(tile, this::getEnergyStored, this::setEnergyStored));
        addMultipleDataSlots(DataSlotUtil.forIntegerSplit(tile, this::getEnergyCapacity, this::setEnergyCapacity));
        addDataSlots(tile.sideConfig.toIIntArray());
    }

    private void addMultipleDataSlots(IntReferenceHolder... holders) {
        for (IntReferenceHolder holder : holders) {
            addDataSlot(holder);
        }
    }

    private void setupContainerInv() {
        int inputSlots = inventory.getInputSlots();
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
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new SlotItemHandler(inventory, j + i * 9 + 9, 8 + j * 18, 72 + i * 18));
            }
        }
        // hot bar
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 130));
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
        return tile.getCapability(CapabilityEnergy.ENERGY);
    }
}
