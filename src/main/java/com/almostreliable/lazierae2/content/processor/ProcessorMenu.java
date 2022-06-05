package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.core.Setup.Menus;
import com.almostreliable.lazierae2.inventory.OutputSlot;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.network.sync.handler.BooleanDataHandler;
import com.almostreliable.lazierae2.network.sync.handler.IntegerDataHandler;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ProcessorMenu extends GenericMenu<ProcessorEntity> {

    private ProcessorInventory processorInventory;

    public ProcessorMenu(int id, ProcessorEntity entity, Inventory menuInventory) {
        super(Menus.PROCESSOR.get(), id, entity, menuInventory);
        entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inv -> {
            processorInventory = (ProcessorInventory) inv;
            setupContainerInventory();
        });
        setupPlayerInventory();
        syncData();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var stack = ItemStack.EMPTY;
        var slot = slots.get(index);

        // check if the slot has an item inside
        if (!slot.hasItem()) return stack;

        var slotStack = slot.getItem();
        stack = slotStack.copy();

        if (index < processorInventory.getSlots()) {
            // from machine to inventory
            if (!moveItemStackTo(
                slotStack,
                processorInventory.getSlots(),
                processorInventory.getSlots() + PLAYER_INV_SIZE,
                false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (GameUtil.isValidUpgrade(slotStack)) {
            // from inventory to upgrade slot
            if (!moveItemStackTo(
                slotStack,
                ProcessorInventory.UPGRADE_SLOT,
                ProcessorInventory.UPGRADE_SLOT + 1,
                false
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            // from inventory to machine inputs
            slotStack = processorInventory.insertToInputs(slotStack);
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

    public boolean hasUpgrades() {
        return getUpgradeCount() > 0;
    }

    @Override
    protected void setupContainerInventory() {
        var inputSlots = processorInventory.getInputSlots();
        addSlot(new UpgradeSlot(this, processorInventory, ProcessorInventory.UPGRADE_SLOT, 8, 50));
        addSlot(new OutputSlot(processorInventory, ProcessorInventory.OUTPUT_SLOT, 116, 29));
        if (inputSlots == 1) {
            addSlot(new SlotItemHandler(processorInventory, 2, 44, 29));
        } else if (inputSlots == 3) {
            addSlot(new SlotItemHandler(processorInventory, 2, 44, 8));
            addSlot(new SlotItemHandler(processorInventory, 3, 44, 29));
            addSlot(new SlotItemHandler(processorInventory, 4, 44, 50));
        } else {
            throw new IllegalArgumentException(f("Invalid input slot getCount: {}", inputSlots));
        }
    }

    @Override
    protected int getSlotY() {
        return 72;
    }

    private void syncData() {
        sync.addDataHandler(new BooleanDataHandler(entity::isAutoExtracting, entity::setAutoExtract));
        sync.addDataHandler(new IntegerDataHandler(entity::getProgress, entity::setProgress));
        sync.addDataHandler(new IntegerDataHandler(entity::getProcessTime, entity::setProcessTime));
        sync.addDataHandler(new IntegerDataHandler(entity::getRecipeTime, entity::setRecipeTime));
        sync.addDataHandler(new IntegerDataHandler(entity::getEnergyCost, entity::setEnergyCost));
        sync.addDataHandler(new IntegerDataHandler(entity::getRecipeEnergy, entity::setRecipeEnergy));
        sync.addDataHandler(entity.energy);
        sync.addDataHandler(entity.sideConfig);
    }

    public int getUpgradeCount() {
        return processorInventory.getUpgradeCount();
    }

    public int getEnergyStored() {
        return entity.energy.getEnergyStored();
    }

    public void setEnergyStored(int energy) {
        entity.energy.setEnergy(energy);
    }

    public int getEnergyCapacity() {
        return entity.energy.getMaxEnergyStored();
    }
}
