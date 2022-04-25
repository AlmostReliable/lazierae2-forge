package com.almostreliable.lazierae2.component;

import com.almostreliable.lazierae2.machine.MachineTile;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class InventoryHandler implements IItemHandlerModifiable, INBTSerializable<CompoundNBT> {

    public static final int NON_INPUT_SLOTS = 2;
    public static final int UPGRADE_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private final MachineTile tile;
    private LazyOptional<IItemHandler> inputInventoryCap;
    private LazyOptional<IItemHandler> outputInventoryCap;
    private LazyOptional<IItemHandler> ioInventoryCap;
    private NonNullList<ItemStack> stacks;
    @Nullable
    private IInventory vanillaInventory;
    private boolean vanillaNeedsUpdate;

    public InventoryHandler(MachineTile tile, int inputSlots) {
        this.tile = tile;
        stacks = NonNullList.withSize(inputSlots + NON_INPUT_SLOTS, ItemStack.EMPTY);
        setupSubInventories(inputSlots);
    }

    public IInventory toVanilla() {
        if (vanillaInventory == null || vanillaNeedsUpdate) {
            vanillaInventory = new RecipeWrapper(this);
            vanillaNeedsUpdate = false;
        }
        return vanillaInventory;
    }

    public void dropContents() {
        if (tile.getLevel() == null) return;
        BlockPos pos = tile.getBlockPos();
        for (int i = OUTPUT_SLOT; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack.isEmpty()) continue;
            tile.getLevel().addFreshEntity(new ItemEntity(tile.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    public CompoundNBT serializeUpgrades() {
        return getStackInSlot(UPGRADE_SLOT).save(new CompoundNBT());
    }

    public void deserializeUpgrades(CompoundNBT nbt) {
        setStackInSlot(UPGRADE_SLOT, ItemStack.of(nbt));
    }

    public void shrinkInputSlots() {
        for (int i = NON_INPUT_SLOTS; i < getSlots(); i++) {
            if (getStackInSlot(i).isEmpty()) continue;
            if (getStackInSlot(i).getCount() == 1) {
                setStackInSlot(i, ItemStack.EMPTY);
            } else {
                getStackInSlot(i).shrink(1);
                tile.setChanged();
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        ListNBT tagList = new ListNBT();
        for (int slot = 0; slot < stacks.size(); slot++) {
            if (!stacks.get(slot).isEmpty()) {
                CompoundNBT itemTag = new CompoundNBT();
                itemTag.putInt("slot", slot);
                stacks.get(slot).save(itemTag);
                tagList.add(itemTag);
            }
        }
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("size", stacks.size());
        tag.put("items", tagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag) {
        setSize(tag.contains("size") ? tag.getInt("size") : stacks.size());
        ListNBT tagList = tag.getList("items", NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("slot");
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
        setupSubInventories(getInputSlots());
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlot(slot);
        stacks.set(slot, stack);
        onContentsChanged();
    }

    public void invalidate() {
        inputInventoryCap.invalidate();
        outputInventoryCap.invalidate();
        ioInventoryCap.invalidate();
    }

    private void setupSubInventories(int inputSlots) {
        InputInventory inputInventory = new InputInventory(this, inputSlots);
        inputInventoryCap = LazyOptional.of(() -> inputInventory);
        OutputInventory outputInventory = new OutputInventory(this);
        outputInventoryCap = LazyOptional.of(() -> outputInventory);
        IOInventory ioInventory = new IOInventory(this, inputSlots + 1);
        ioInventoryCap = LazyOptional.of(() -> ioInventory);
    }

    private void onContentsChanged() {
        vanillaNeedsUpdate = true;
        tile.setChanged();
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new IllegalStateException(f("Slot {} is not in range [0,{})", slot, stacks.size()));
        }
    }

    private int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    public LazyOptional<IItemHandler> getInputInventoryCap() {
        return inputInventoryCap;
    }

    public LazyOptional<IItemHandler> getOutputInventoryCap() {
        return outputInventoryCap;
    }

    public LazyOptional<IItemHandler> getIoInventoryCap() {
        return ioInventoryCap;
    }

    public int getInputSlots() {
        return getSlots() - NON_INPUT_SLOTS;
    }

    public ItemStack getStackInOutput() {
        return getStackInSlot(OUTPUT_SLOT);
    }

    public void setStackInOutput(ItemStack stack) {
        setStackInSlot(OUTPUT_SLOT, stack);
    }

    public int getUpgradeCount() {
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

        ItemStack current = stacks.get(slot);
        int limit = getStackLimit(slot, stack);
        if (!current.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, current)) return stack;
            limit -= current.getCount();
        }
        if (limit <= 0) return stack;

        boolean reachedLimit = stack.getCount() > limit;
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

        ItemStack current = stacks.get(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        int toExtract = Math.min(amount, current.getMaxStackSize());

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
        if (slot == UPGRADE_SLOT) return tile.getMachineType().getUpgradeSlots();
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    private void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    private static final class InputInventory implements IItemHandlerModifiable {

        private final InventoryHandler parent;
        private final int size;

        private InputInventory(InventoryHandler parent, int size) {
            this.parent = parent;
            this.size = size;
        }

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

        final InventoryHandler parent;

        private OutputInventory(InventoryHandler parent) {
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

        private IOInventory(InventoryHandler parent, int size) {
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
