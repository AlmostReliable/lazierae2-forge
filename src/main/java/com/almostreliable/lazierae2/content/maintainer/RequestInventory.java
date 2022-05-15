package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class RequestInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    private final MaintainerEntity entity;
    private final Request[] requests;

    RequestInventory(
        MaintainerEntity entity, int slots
    ) {
        this.entity = entity;
        requests = new Request[slots];
        for (var i = 0; i < slots; i++) {
            requests[i] = new Request(i);
        }
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide) return;
        validateSlot(slot);
        requests[slot].updateStack(stack);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var i = 0; i < getSlots(); i++) {
            tag.put(String.valueOf(i), requests[i].serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var i = 0; i < getSlots(); i++) {
            requests[i].deserializeNBT(tag.getCompound(String.valueOf(i)));
        }
    }

    public Request get(int slot) {
        return requests[slot];
    }

    int firstAvailableSlot() {
        for (var slot = 0; slot < requests.length; slot++) {
            var request = requests[slot];
            if (request.stack.isEmpty()) return slot;
        }
        return -1;
    }

    boolean matches(int slot, AEKey what) {
        return what.matches(GenericStack.fromItemStack(requests[slot].stack));
    }

    private void validateSlot(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IllegalArgumentException("Slot " + slot + " is out of range");
        }
    }

    @Override
    public int getSlots() {
        return requests.length;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    public final class Request implements INBTSerializable<CompoundTag> {

        private final int slot;
        private boolean state = true;
        private ItemStack stack = ItemStack.EMPTY;
        private long count;
        private long batch = 1;

        private Request(int slot) {
            this.slot = slot;
        }

        public GenericStack toGenericStack(long count) {
            var stackCopy = stack.copy();
            return new GenericStack(Objects.requireNonNull(AEItemKey.of(stackCopy)), count);
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putBoolean("getState", state);
            tag.put("getStack", stack.serializeNBT());
            tag.putLong("getCount", count);
            tag.putLong("getBatch", batch);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            state = nbt.getBoolean("getState");
            stack = ItemStack.of(nbt.getCompound("getStack"));
            count = nbt.getLong("getCount");
            batch = nbt.getLong("getBatch");
        }

        public void updateState(boolean state) {
            if (this.state != state) {
                this.state = state;
                entity.setChanged();
            }
        }

        public void updateCount(long count) {
            var oldStack = stack;
            var oldCount = this.count;
            var oldBatch = batch;
            if (stack.isEmpty() || count <= 0) {
                resetSlot();
            } else {
                this.count = count;
            }
            if (!oldStack.sameItem(stack) || oldCount != this.count || oldBatch != batch) entity.setChanged();
        }

        public void updateBatch(long batch) {
            var oldBatch = this.batch;
            this.batch = batch <= 0 ? 1 : batch;
            if (oldBatch != this.batch) entity.setChanged();
        }

        @Override
        public String toString() {
            return "Request[" + "state=" + state + ", " + "stack=" + stack + ", " + "count=" + count + ", " + "batch=" +
                batch + ']';
        }

        void updateStackClient(ItemStack stack) {
            if (entity.getLevel() == null || !entity.getLevel().isClientSide) return;
            this.stack = stack;
        }

        private void updateStack(ItemStack stack) {
            if (stack.isEmpty()) {
                resetSlot();
            } else {
                count = stack.getCount();
                batch = 1;
                this.stack = stack;
                stack.setCount(1);
                stackChanged();
            }
        }

        private void stackChanged() {
            entity.getStorageManager().clear(slot);
            entity.setChanged();
        }

        private void resetSlot() {
            var oldStack = stack;
            stack = ItemStack.EMPTY;
            count = 0;
            batch = 1;
            if (!oldStack.isEmpty()) stackChanged();
        }

        public boolean getState() {
            return state;
        }

        public ItemStack getStack() {
            return stack;
        }

        public long getCount() {
            return count;
        }

        long getBatch() {
            return batch;
        }

        public boolean isRequesting() {
            return state && !stack.isEmpty();
        }
    }
}
