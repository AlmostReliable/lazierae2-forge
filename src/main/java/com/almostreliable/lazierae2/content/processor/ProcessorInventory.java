package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ProcessorInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    public static final int NON_INPUT_SLOTS = 2;
    static final int UPGRADE_SLOT = 0;
    static final int OUTPUT_SLOT = 1;
    private final ProcessorEntity entity;
    private final int inputSlots;
    private LazyOptional<IItemHandler> inputInventoryCap;
    private LazyOptional<IItemHandler> outputInventoryCap;
    private LazyOptional<IItemHandler> ioInventoryCap;
    private NonNullList<ItemStack> stacks;
    @Nullable
    private Container vanillaInventory;
    private boolean vanillaNeedsUpdate;

    ProcessorInventory(ProcessorEntity entity) {
        this.entity = entity;
        inputSlots = entity.getProcessorType().getInputSlots();
        stacks = NonNullList.withSize(inputSlots + NON_INPUT_SLOTS, ItemStack.EMPTY);
        setupSubInventories();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tagList = new ListTag();
        for (var slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                var itemTag = new CompoundTag();
                itemTag.putInt(SLOT_ID, slot);
                stacks.get(slot).save(itemTag);
                tagList.add(itemTag);
            }
        }
        var tag = new CompoundTag();
        tag.putInt(SIZE_ID, stacks.size());
        tag.put(ITEMS_ID, tagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        setSize(tag.contains(SIZE_ID) ? tag.getInt(SIZE_ID) : stacks.size());
        var tagList = tag.getList(ITEMS_ID, Tag.TAG_COMPOUND);
        for (var i = 0; i < tagList.size(); i++) {
            var itemTags = tagList.getCompound(i);
            var slot = itemTags.getInt(SLOT_ID);
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
        setupSubInventories();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlot(slot);
        stacks.set(slot, stack);
        onContentsChanged();
    }

    Container toVanilla() {
        if (vanillaInventory == null || vanillaNeedsUpdate) {
            vanillaInventory = new RecipeWrapper(this);
            vanillaNeedsUpdate = false;
        }
        return vanillaInventory;
    }

    void dropContents(boolean creative) {
        if (entity.getLevel() == null) return;
        var pos = entity.getBlockPos();
        for (var slot = creative ? 0 : OUTPUT_SLOT; slot < getSlots(); slot++) {
            var stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            entity
                .getLevel()
                .addFreshEntity(new ItemEntity(entity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
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
                entity.setChanged();
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

    int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    private void setupSubInventories() {
        inputInventoryCap = LazyOptional.of(() -> new InputInventory(this, inputSlots));
        outputInventoryCap = LazyOptional.of(() -> new OutputInventory(this));
        ioInventoryCap = LazyOptional.of(() -> new IOInventory(this, inputSlots + 1));
    }

    private void onContentsChanged() {
        vanillaNeedsUpdate = true;
        entity.setChanged();
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, stacks.size()));
        }
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

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return stacks.get(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        validateSlot(slot);

        var current = stacks.get(slot);
        var limit = getStackLimit(slot, stack);
        if (!current.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, current)) return stack;
            limit -= current.getCount();
        }
        if (limit <= 0) return stack;

        var reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (current.isEmpty()) {
                stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                current.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged();
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        validateSlot(slot);

        var current = stacks.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        var toExtract = Math.min(amount, current.getMaxStackSize());

        if (current.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged();
                return current;
            }
            return current.copy();
        }
        if (!simulate) {
            stacks.set(slot, ItemHandlerHelper.copyStackWithSize(current, current.getCount() - toExtract));
            onContentsChanged();
        }

        return ItemHandlerHelper.copyStackWithSize(current, toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        if (slot == UPGRADE_SLOT) return entity.getProcessorType().getUpgradeSlots();
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot != OUTPUT_SLOT && slot != UPGRADE_SLOT && inputSlots > 1) {
            for (var inputSlot = NON_INPUT_SLOTS; inputSlot < inputSlots + NON_INPUT_SLOTS; inputSlot++) {
                if (inputSlot == slot) continue;
                if (stacks.get(inputSlot).sameItem(stack)) return false;
            }
        }
        return true;
    }

    private void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
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
