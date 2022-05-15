package com.almostreliable.lazierae2.util;

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class DataSlotUtil {

    private DataSlotUtil() {}

    /**
     * Utility method to create a new {@link DataSlot} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with the maximum value of 2^15-1.
     *
     * @param entity The block entity to mark for saving.
     * @param s      The supplier of the integer value.
     * @param c      The consumer of the integer value.
     * @return The new {@link DataSlot}.
     */
    public static DataSlot forInteger(BlockEntity entity, IntSupplier s, IntConsumer c) {
        return new SyncedDataSlot() {

            @Override
            public int get() {
                return s.getAsInt();
            }

            @Override
            public void set(int value) {
                c.accept(value);
                entity.setChanged();
            }
        };
    }

    private abstract static class SyncedDataSlot extends DataSlot {

        private boolean initSync = true;

        @Override
        public boolean checkAndClearUpdateFlag() {
            if (initSync) {
                initSync = false;
                return true;
            }
            return super.checkAndClearUpdateFlag();
        }
    }
}
