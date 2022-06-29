package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.content.GenericInventory;
import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ProcessorInventory extends GenericInventory<ProcessorEntity> {

    public static final int NON_INPUT_SLOTS = 2;
    static final int UPGRADE_SLOT = 0;
    static final int OUTPUT_SLOT = 1;
    private final int inputSlots;
    private LazyOptional<IItemHandler> inputInventoryCap;
    private LazyOptional<IItemHandler> outputInventoryCap;
    private LazyOptional<IItemHandler> ioInventoryCap;

    ProcessorInventory(ProcessorEntity entity) {
        super(entity, entity.getProcessorType().getInputSlots() + NON_INPUT_SLOTS);
        inputSlots = entity.getProcessorType().getInputSlots();
        setupSubInventories();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        setupSubInventories();
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        if (slot == UPGRADE_SLOT) return owner.getProcessorType().getUpgradeSlots();
        return super.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot != OUTPUT_SLOT && slot != UPGRADE_SLOT && inputSlots > 1) {
            for (var inputSlot = NON_INPUT_SLOTS; inputSlot < inputSlots + NON_INPUT_SLOTS; inputSlot++) {
                if (inputSlot == slot) continue;
                if (getStackInSlot(inputSlot).sameItem(stack)) return false;
            }
        }
        return super.isItemValid(slot, stack);
    }

    void dropContents(boolean creative) {
        if (owner.getLevel() == null) return;
        var pos = owner.getBlockPos();
        for (var slot = creative ? 0 : OUTPUT_SLOT; slot < getSlots(); slot++) {
            var stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            owner.getLevel()
                .addFreshEntity(new ItemEntity(owner.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    LazyOptional<IItemHandler> getInventoryCap(IO_SETTING type) {
        return switch (type) {
            case INPUT -> inputInventoryCap;
            case OUTPUT -> outputInventoryCap;
            case IO -> ioInventoryCap;
            default -> throw new IllegalArgumentException(f("Unknown inventory type: {}", type));
        };
    }

    CompoundTag serializeUpgrades() {
        return getStackInSlot(UPGRADE_SLOT).save(new CompoundTag());
    }

    void deserializeUpgrades(CompoundTag tag) {
        setStackInSlot(UPGRADE_SLOT, ItemStack.of(tag));
    }

    void shrinkInputSlots(Map<Integer, Integer> slotsToShrink, int outputMultiplier) {
        for (var slot : slotsToShrink.entrySet()) {
            if (getStackInSlot(slot.getKey()).getCount() == slot.getValue() * outputMultiplier) {
                setStackInSlot(slot.getKey(), ItemStack.EMPTY);
            } else {
                getStackInSlot(slot.getKey()).shrink(slot.getValue() * outputMultiplier);
                owner.setChanged();
            }
        }
    }

    void invalidate() {
        inputInventoryCap.invalidate();
        outputInventoryCap.invalidate();
        ioInventoryCap.invalidate();
    }

    ItemStack insertToInputs(ItemStack stack) {
        if (stack.isEmpty()) return stack;

        // move non stackables to the first available slot
        if (!stack.isStackable()) {
            for (var slot = NON_INPUT_SLOTS; slot < Math.min(getSlots(), NON_INPUT_SLOTS + inputSlots); slot++) {
                stack = insertItem(slot, stack, false);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
            return stack;
        }

        // fill up existing stacks
        for (var slot = NON_INPUT_SLOTS; slot < Math.min(getSlots(), NON_INPUT_SLOTS + inputSlots); slot++) {
            var slotStack = getStackInSlot(slot);
            if (ItemHandlerHelper.canItemStacksStackRelaxed(slotStack, stack)) {
                var oldCount = stack.getCount();
                stack = insertItem(slot, stack, false);
                if (stack.isEmpty() || oldCount != stack.getCount()) return stack;
            }
        }

        // insert remainder into empty slot
        if (!stack.isEmpty()) {
            for (var slot = NON_INPUT_SLOTS; slot < Math.min(getSlots(), NON_INPUT_SLOTS + inputSlots); slot++) {
                if (getStackInSlot(slot).isEmpty()) {
                    stack = insertItem(slot, stack, false);
                    if (stack.isEmpty()) return stack;
                }
            }
        }

        return stack;
    }

    private void setupSubInventories() {
        inputInventoryCap = LazyOptional.of(() -> new InputInventory(this, inputSlots));
        outputInventoryCap = LazyOptional.of(() -> new OutputInventory(this));
        ioInventoryCap = LazyOptional.of(() -> new IOInventory(this, inputSlots + 1));
    }

    int getInputSlots() {
        return inputSlots;
    }

    ItemStack getStackInOutput() {
        return getStackInSlot(OUTPUT_SLOT);
    }

    void setStackInOutput(ItemStack stack) {
        setStackInSlot(OUTPUT_SLOT, stack);
    }

    int getUpgradeCount() {
        return getStackInSlot(UPGRADE_SLOT).getCount();
    }

    private record InputInventory(ProcessorInventory parent, int size) implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            parent.setStackInSlot(slot + NON_INPUT_SLOTS, stack);
        }

        @Override
        public int getSlots() {
            return size;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return parent.getStackInSlot(slot + NON_INPUT_SLOTS);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return parent.insertItem(slot + NON_INPUT_SLOTS, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return parent.getSlotLimit(slot + NON_INPUT_SLOTS);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return parent.isItemValid(slot + NON_INPUT_SLOTS, stack);
        }
    }

    private static class OutputInventory implements IItemHandlerModifiable {

        final ProcessorInventory parent;

        private OutputInventory(ProcessorInventory parent) {
            this.parent = parent;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            parent.setStackInSlot(slot + OUTPUT_SLOT, stack);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return parent.getStackInSlot(slot + OUTPUT_SLOT);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return parent.extractItem(slot + OUTPUT_SLOT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return parent.getSlotLimit(slot + OUTPUT_SLOT);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return parent.isItemValid(slot + OUTPUT_SLOT, stack);
        }
    }

    private static final class IOInventory extends OutputInventory {

        private final int size;

        private IOInventory(ProcessorInventory parent, int size) {
            super(parent);
            this.size = size;
        }

        @Override
        public int getSlots() {
            return size;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot + OUTPUT_SLOT < NON_INPUT_SLOTS) return stack;
            return parent.insertItem(slot + OUTPUT_SLOT, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot + OUTPUT_SLOT != OUTPUT_SLOT) return ItemStack.EMPTY;
            return parent.extractItem(slot + OUTPUT_SLOT, amount, simulate);
        }
    }
}
