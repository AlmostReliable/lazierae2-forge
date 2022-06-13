package com.almostreliable.lazierae2.content;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class GenericInventory<E extends GenericEntity> implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    protected final E owner;
    private NonNullList<ItemStack> stacks;

    protected GenericInventory(E owner, int size) {
        this.owner = owner;
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    protected GenericInventory(E owner) {
        this(owner, 0);
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
        if (tag.contains(SIZE_ID)) setSize(tag.getInt(SIZE_ID));
        if (tag.contains(ITEMS_ID)) {
            var tagList = tag.getList(ITEMS_ID, Tag.TAG_COMPOUND);
            for (var i = 0; i < tagList.size(); i++) {
                var itemTags = tagList.getCompound(i);
                var slot = itemTags.getInt(SLOT_ID);
                if (slot >= 0 && slot < stacks.size()) {
                    stacks.set(slot, ItemStack.of(itemTags));
                }
            }
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlot(slot);
        stacks.set(slot, stack);
        onContentsChanged();
    }

    public int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    protected void validateSlot(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IndexOutOfBoundsException(f("Slot {} is not in range [0,{})", slot, stacks.size()));
        }
    }

    protected void onContentsChanged() {
        owner.setChanged();
    }

    protected void setSize(int size, boolean preserve) {
        if (preserve) {
            var list = NonNullList.withSize(size, ItemStack.EMPTY);
            for (var i = 0; i < stacks.size(); i++) {
                if (i < list.size()) list.set(i, stacks.get(i));
            }
            stacks = list;
        } else {
            stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        }
        owner.setChanged();
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
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        validateSlot(slot);
        return true;
    }

    protected void setSize(int size) {
        setSize(size, false);
    }
}
