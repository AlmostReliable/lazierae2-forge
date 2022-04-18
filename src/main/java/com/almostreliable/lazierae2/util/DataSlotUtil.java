package com.almostreliable.lazierae2.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

public final class DataSlotUtil {

    private static final int UPPER = 0xFFFF_0000;
    private static final int LOWER = 0x0000_FFFF;

    private DataSlotUtil() {}

    /**
     * Utility method to create a new {@link DataSlot} for boolean values without
     * the need to have a clunky anonymous class and conversion.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     *
     * @param entity The block entity to mark for saving.
     * @param s      The supplier of the boolean value.
     * @param c      The consumer of the boolean value.
     * @return The new {@link DataSlot}.
     */
    public static DataSlot forBoolean(BlockEntity entity, BooleanSupplier s, BooleanConsumer c) {
        return new SyncedDataSlot() {

            @Override
            public int get() {
                return s.getAsBoolean() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                c.accept(value == 1);
                entity.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link DataSlot} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with the maximum value of 2^15-1.
     * For larger values, use {@link #forIntegerSplit(BlockEntity, IntSupplier, IntConsumer)}.
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

    /**
     * Utility method to create a new {@link ContainerData} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with potential higher values than 2^15-1.
     * For smaller values use {@link #forInteger(BlockEntity, IntSupplier, IntConsumer)}.
     *
     * @param entity The tile entity to mark for saving.
     * @param s      The supplier of the integer value.
     * @param c      The consumer of the integer value.
     * @return The new {@link ContainerData}.
     */
    public static DataSlot[] forIntegerSplit(BlockEntity entity, IntSupplier s, IntConsumer c) {
        return containerDataToDataSlot(new ContainerData() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return (s.getAsInt() >> 16) & LOWER;
                }

                return s.getAsInt() & LOWER;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    c.accept((s.getAsInt() & LOWER) | (value << 16));
                } else {
                    c.accept((s.getAsInt() & UPPER) + (value & LOWER));
                }
                entity.setChanged();
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    private static SyncedDataSlot[] containerDataToDataSlot(ContainerData containerData) {
        return IntStream
            .range(0, containerData.getCount())
            .mapToObj(i -> SyncedDataSlot.of(containerData, i))
            .toArray(SyncedDataSlot[]::new);
    }

    private abstract static class SyncedDataSlot extends DataSlot {

        private boolean initSync = true;

        private static SyncedDataSlot of(ContainerData data, int index) {
            return new SyncedDataSlot() {
                @Override
                public int get() {
                    return data.get(index);
                }

                @Override
                public void set(int value) {
                    data.set(index, value);
                }
            };
        }

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
