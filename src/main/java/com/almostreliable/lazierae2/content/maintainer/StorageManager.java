package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class StorageManager implements IStorageWatcherNode, INBTSerializable<CompoundTag> {

    private final Storage[] storages;
    private final MaintainerEntity owner;
    @Nullable
    private IStackWatcher stackWatcher;

    public StorageManager(MaintainerEntity owner, int slots) {
        this.owner = owner;
        storages = new Storage[slots];
    }

    public Storage get(int slot) {
        if (storages[slot] == null) {
            storages[slot] = new Storage();
        }
        return storages[slot];
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        stackWatcher = newWatcher;
        resetWatcher();
    }

    private void resetWatcher() {
        if (stackWatcher != null) {
            stackWatcher.reset();
            populateWatcher(stackWatcher);
        }
    }

    public void populateWatcher(IStackWatcher watcher) {
        for (var slot = 0; slot < storages.length; slot++) {
            if (!owner.craftRequests.get(slot).stack().isEmpty()) {
                watcher.add(AEItemKey.of(owner.craftRequests.get(slot).stack()));
            }
        }
    }

    @Override
    public void onStackChange(AEKey what, long amount) {
        for (int slot = 0; slot < storages.length; slot++) {
            if (owner.craftRequests.matches(slot, what)) {
                get(slot).knownAmount = amount;
                get(slot).pendingAmount = 0;
            }
        }
    }

    public void clear(int slot) {
        get(slot).knownAmount = -1;
        calcSlotAmount(slot);
        resetWatcher();
    }

    public long computeDelta(int slot) {
        var request = owner.craftRequests.get(slot);
        if (request.stack().isEmpty()) {
            return 0;
        }

        long delta = get(slot).knownAmount + get(slot).pendingAmount;
        if (delta < request.count()) {
            return request.batch();
        }
        return 0;
    }

    private void calcSlotAmount(int slot) {
        // TODO CHECK
        var request = owner.craftRequests.get(slot);
        if (request.stack().isEmpty()) {
            return;
        }
        GenericStack genericStack = GenericStack.fromItemStack(request.stack());
        if (genericStack == null) {
            return;
        }
        get(slot).knownAmount = owner
            .getMainNodeGrid()
            .getStorageService()
            .getInventory()
            .getAvailableStacks()
            .get(genericStack.what());
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (int slot = 0; slot < storages.length; slot++) {
            tag.put(String.valueOf(slot), get(slot).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (int slot = 0; slot < storages.length; slot++) {
            get(slot).deserializeNBT(tag.getCompound(String.valueOf(slot)));
        }
    }

    public class Storage implements INBTSerializable<CompoundTag> {
        @Nullable
        private AEKey key;
        private long buffer;
        private long pendingAmount;
        private long knownAmount = -1;

        public void update(AEKey key, long buffer) {
            if (this.key != null && !key.fuzzyEquals(this.key, FuzzyMode.IGNORE_ALL)) {
                throw new IllegalArgumentException("Key mismatch");
            }

            this.key = key;
            this.buffer += buffer;
            System.out.printf("%s: %d\n", this.key, this.buffer);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (key != null) tag.put("key", key.toTagGeneric());
            tag.putLong("buffer", buffer);
            tag.putLong("pendingAmount", pendingAmount);
            tag.putLong("knownAmount", knownAmount);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("key")) key = AEKey.fromTagGeneric(tag.getCompound("key"));
            if (tag.contains("buffer")) buffer = tag.getLong("buffer");
            if (tag.contains("pendingAmount")) pendingAmount = tag.getLong("pendingAmount");
            if (tag.contains("knownAmount")) knownAmount = tag.getLong("knownAmount");
        }

        @Nullable
        public AEKey getKey() {
            return key;
        }

        public long getBuffer() {
            return getKey() == null ? 0 : buffer;
        }

        /**
         * @param inserted - amount of items inserted into the system
         * @return true if the buffer is not empty
         */
        public boolean compute(long inserted) {
            pendingAmount = inserted;
            buffer = getBuffer() - inserted;
            if (buffer == 0) {
                System.out.print("BUFFER CLEARED");
                key = null;
            }
            return buffer > 0;
        }

        public long getKnownAmount() {
            return knownAmount;
        }
    }
}
