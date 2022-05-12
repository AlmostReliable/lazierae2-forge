package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket.SYNC_FLAGS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

// TODO: use ItemHandler interface instead of ItemStackHandler to avoid internal storage
public final class RequestInventory extends ItemStackHandler {

    private final MaintainerEntity entity;
    private final int slots;
    private final Request[] requests;

    RequestInventory(
        MaintainerEntity entity, int slots
    ) {
        this.entity = entity;
        this.slots = slots;
        requests = new Request[slots];
        Arrays.fill(requests, new Request(true, ItemStack.EMPTY, 0, 1));
    }

    public void updateCount(int slot, long count) {
        validateSlot(slot);
        var oldRequest = requests[slot];
        if (requests[slot].stack.isEmpty()) {
            requests[slot] = new Request(requests[slot].state, ItemStack.EMPTY, 0, 1);
        } else if (count <= 0) {
            setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            requests[slot] = new Request(requests[slot].state, requests[slot].stack, count, requests[slot].batch);
        }
        if (!oldRequest.equals(requests[slot])) entity.setChanged();
    }

    public void updateBatch(int slot, long batch) {
        validateSlot(slot);
        var oldRequest = requests[slot];
        if (batch <= 0) {
            requests[slot] = new Request(requests[slot].state, requests[slot].stack, requests[slot].count, 1);
        } else {
            requests[slot] = new Request(requests[slot].state, requests[slot].stack, requests[slot].count, batch);
        }
        if (!oldRequest.equals(requests[slot])) entity.setChanged();
    }

    public void updateState(int slot, boolean enabled) {
        validateSlot(slot);
        var oldState = requests[slot].state;
        if (oldState != enabled) {
            requests[slot] = new Request(enabled, requests[slot].stack, requests[slot].count, requests[slot].batch);
            entity.setChanged();
        }
    }

    public void updateStackClient(int slot, ItemStack stack) {
        if (entity.getLevel() == null || !entity.getLevel().isClientSide) return;
        requests[slot] = new Request(requests[slot].state, stack, requests[slot].count, requests[slot].batch);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide) return;
        validateSlot(slot);
        var oldRequest = requests[slot];
        var flags = 0;
        if (stack.isEmpty()) {
            requests[slot] = new Request(requests[slot].state, ItemStack.EMPTY, 0, 1);
            flags |= SYNC_FLAGS.STACK | SYNC_FLAGS.COUNT | SYNC_FLAGS.BATCH;
        } else {
            var count = stack.getCount();
            stack.setCount(1);
            requests[slot] = new Request(requests[slot].state, stack, count, requests[slot].batch);
            flags |= SYNC_FLAGS.STACK | SYNC_FLAGS.COUNT;
        }
        if (!ItemStack.isSame(oldRequest.stack, requests[slot].stack)) {
            entity.getStorageManager().clear(slot);
        }
        if (!oldRequest.equals(requests[slot])) {
            entity.syncData(slot, flags);
            entity.setChanged();
        }
    }

    @Override
    public int getSlots() {
        return slots;
    }

    // TODO: remove this and use the getter
    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return requests[slot].stack;
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

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var i = 0; i < slots; i++) {
            tag.put(String.valueOf(i), requests[i].serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var i = 0; i < slots; i++) {
            requests[i] = Request.deserializeNBT(tag.getCompound(String.valueOf(i)));
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
        if (slot < 0 || slot >= slots) {
            throw new IllegalArgumentException("Slot " + slot + " is out of range");
        }
    }

    // TODO: make this own class and mutable
    public record Request(boolean state, ItemStack stack, long count, long batch) {

        private static Request deserializeNBT(CompoundTag tag) {
            return new Request(
                tag.getBoolean("state"),
                ItemStack.of(tag.getCompound("item")),
                tag.getInt("count"),
                tag.getInt("batch")
            );
        }

        public GenericStack toGenericStack(long count) {
            var stackCopy = stack.copy();
            return new GenericStack(Objects.requireNonNull(AEItemKey.of(stackCopy)), count);
        }

        private CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putBoolean("state", state);
            tag.put("item", stack.serializeNBT());
            tag.putInt("count", (int) count);
            tag.putInt("batch", (int) batch);
            return tag;
        }

        public boolean isRequesting() {
            return state && !stack.isEmpty();
        }
    }
}
