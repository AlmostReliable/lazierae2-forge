package com.almostreliable.lazierae2.component;

import appeng.api.networking.IStackWatcher;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.machine.MachineEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.util.MathUtil;
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

    public static class MachineInventory extends InventoryHandler<MachineEntity> {

        public static final int NON_INPUT_SLOTS = 2;
        public static final int UPGRADE_SLOT = 0;
        public static final int OUTPUT_SLOT = 1;

        public MachineInventory(MachineEntity entity) {
            super(entity, entity.getMachineType().getInputSlots() + NON_INPUT_SLOTS);
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

        public void updateCount(int slot, long count) {
            if (slot >= 0 && slot < slots) {
                if (count > 0) {
                    if (!requests[slot].stack.isEmpty()) {
                        requests[slot] = new Request(requests[slot].state,
                            requests[slot].stack,
                            count,
                            requests[slot].batch
                        );
                        clearSlot(slot);
                        entity.setChanged();
                    }
                } else if (!requests[slot].stack.isEmpty()) {
                    requests[slot] = new Request(requests[slot].state, ItemStack.EMPTY, 0, requests[slot].batch);
                    clearSlot(slot);
                    entity.setChanged();
                }
            }
        }

        public void updateBatch(int slot, long batch) {
            if (slot >= 0 && slot < slots && batch >= 0) {
                requests[slot] = new Request(requests[slot].state, requests[slot].stack, requests[slot].count, batch);
                entity.setChanged();
            }
        }

        public void updateState(int slot, boolean enabled) {
            if (slot >= 0 && slot < slots) {
                var oldState = requests[slot].state;
                requests[slot] = new Request(enabled, requests[slot].stack, requests[slot].count, requests[slot].batch);
                if (oldState != enabled) {
                    entity.setChanged();
                }
            }
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (entity.getLevel() == null || entity.getLevel().isClientSide) return;
            if (stack.isEmpty()) {
                requests[slot] = new Request(requests[slot].state, ItemStack.EMPTY, 0, requests[slot].batch);
            } else {
                requests[slot] = new Request(requests[slot].state, stack, 1, requests[slot].batch);
                stack.setCount(1);
            }
            clearSlot(slot);
            entity.syncClient();
            entity.setChanged();
        }

        @Override
        public int getSlots() {
            return slots;
        }

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

        public boolean matches(int slot, AEKey what) {
            return what.matches(GenericStack.fromItemStack(requests[slot].stack));
        }

        public long computeDelta(int slot, long existing) {
            return requests[slot].stack.isEmpty() ? 0 :
                MathUtil.clamp(requests[slot].count - existing, 0, requests[slot].batch);
        }

        public GenericStack request(int slot, int count) {
            var stack = requests[slot].stack.copy();
            stack.setCount(count);
            return Objects.requireNonNull(GenericStack.fromItemStack(stack));
        }

        public boolean isRequesting(int slot) {
            return !requests[slot].stack.isEmpty();
        }

        public void populateWatcher(IStackWatcher watcher) {
            for (var i = 0; i < slots; i++) {
                if (!requests[i].stack.isEmpty()) {
                    watcher.add(AEItemKey.of(requests[i].stack));
                }
            }
        }

        public long getCount(int slot) {
            return requests[slot].count;
        }

        public long getBatch(int slot) {
            return requests[slot].batch;
        }

        public boolean getState(int slot) {
            return requests[slot].state;
        }

        private void clearSlot(int slot) {
            entity.knownStorageAmounts[slot] = -1;
            entity.resetWatcher();
        }

        public boolean isRequesting() {
            for (var i = 0; i < slots; i++) {
                if (isRequesting(i)) return true;
            }
            return false;
        }

        @SuppressWarnings("ClassCanBeRecord")
        private static final class Request {

            private final boolean state;
            private final ItemStack stack;
            private final long count;
            private final long batch;

            private Request(boolean state, ItemStack stack, long count, long batch) {
                this.state = state;
                this.stack = stack;
                this.count = count;
                this.batch = batch;
            }

            private static Request deserializeNBT(CompoundTag tag) {
                return new Request(tag.getBoolean("state"),
                    ItemStack.of(tag.getCompound("item")),
                    tag.getInt("count"),
                    tag.getInt("batch")
                );
            }

            private CompoundTag serializeNBT() {
                var tag = new CompoundTag();
                tag.putBoolean("state", state);
                tag.put("item", stack.serializeNBT());
                tag.putInt("count", (int) count);
                tag.putInt("batch", (int) batch);
                return tag;
            }
        }
    }
}
