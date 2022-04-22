package com.almostreliable.lazierae2.component;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.CenterEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.processor.ProcessorEntity;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket.SYNC_FLAGS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class InventoryHandler<E extends GenericEntity> extends ItemStackHandler {

    protected final E entity;
    private Container vanillaInventory;
    private boolean vanillaNeedsChange;

    InventoryHandler(E entity, int size) {
        super(size);
        this.entity = entity;
    }

    public Container toVanilla() {
        if (vanillaInventory == null || vanillaNeedsChange) {
            vanillaInventory = new RecipeWrapper(this);
            vanillaNeedsChange = false;
        }
        return vanillaInventory;
    }

    @Override
    protected void onContentsChanged(int slot) {
        vanillaNeedsChange = true;
        entity.setChanged();
    }

    public static class PatternInventory extends InventoryHandler<CenterEntity> {
        public PatternInventory(CenterEntity entity) {
            super(entity, calculateSize(entity));
        }

        private static int calculateSize(CenterEntity entity) {
            return switch (entity.getProcessorType()) {
                case ACCELERATOR -> 0;
                case TIER_1 -> 9;
                case TIER_2 -> 18;
                case TIER_3 -> 27;
            };
        }
    }

    public static class ProcessorInventory extends InventoryHandler<ProcessorEntity> {

        public static final int NON_INPUT_SLOTS = 2;
        public static final int UPGRADE_SLOT = 0;
        public static final int OUTPUT_SLOT = 1;

        public ProcessorInventory(ProcessorEntity entity) {
            super(entity, entity.getProcessorType().getInputSlots() + NON_INPUT_SLOTS);
        }

        public void dropContents() {
            if (entity.getLevel() == null) return;
            var pos = entity.getBlockPos();
            for (var i = OUTPUT_SLOT; i < getSlots(); i++) {
                var stack = getStackInSlot(i);
                if (stack.isEmpty()) continue;
                entity
                    .getLevel()
                    .addFreshEntity(new ItemEntity(entity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
            }
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < NON_INPUT_SLOTS) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        public CompoundTag serializeUpgrades() {
            return getStackInSlot(UPGRADE_SLOT).save(new CompoundTag());
        }

        public void deserializeUpgrades(CompoundTag tag) {
            setStackInSlot(UPGRADE_SLOT, ItemStack.of(tag));
        }

        public void shrinkInputSlots() {
            for (var i = NON_INPUT_SLOTS; i < getSlots(); i++) {
                if (getStackInSlot(i).isEmpty()) continue;
                if (getStackInSlot(i).getCount() == 1) {
                    setStackInSlot(i, ItemStack.EMPTY);
                } else {
                    getStackInSlot(i).shrink(1);
                    entity.setChanged();
                }
            }
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
    }

    public static final class RequestInventory extends InventoryHandler<MaintainerEntity> {

        private final int slots;
        private final Request[] requests;

        public RequestInventory(
            MaintainerEntity parent, int slots
        ) {
            super(parent, slots);
            this.slots = slots;
            requests = new Request[slots];
            Arrays.fill(requests, new Request(true, ItemStack.EMPTY, 0, 1));
        }

        public int firstAvailableSlot() {
            for (var slot = 0; slot < requests.length; slot++) {
                var request = requests[slot];
                if (request.stack.isEmpty()) return slot;
            }
            return -1;
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

        boolean matches(int slot, AEKey what) {
            return what.matches(GenericStack.fromItemStack(requests[slot].stack));
        }

        private void validateSlot(int slot) {
            if (slot < 0 || slot >= slots) {
                throw new IllegalArgumentException("Slot " + slot + " is out of range");
            }
        }

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
}
