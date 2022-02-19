package com.github.almostreliable.lazierae2.util;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

public final class DataSlotUtil {

    private static final int UPPER = 0xFFFF_0000;
    private static final int LOWER = 0x0000_FFFF;

    private DataSlotUtil() {}

    /**
     * Utility method to create a new {@link IntReferenceHolder} for boolean values without
     * the need to have a clunky anonymous class and conversion.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     *
     * @param tile The tile entity to mark for saving.
     * @param s    The supplier of the boolean value.
     * @param c    The consumer of the boolean value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forBoolean(TileEntity tile, BooleanSupplier s, BooleanConsumer c) {
        return new SyncedIntReferenceHolder() {

            @Override
            public int get() {
                return s.getAsBoolean() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                c.accept(value == 1);
                tile.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link IntReferenceHolder} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with the maximum value of 2^15-1.
     * For larger values, use {@link #forIntegerSplit(TileEntity, IntSupplier, IntConsumer)}.
     *
     * @param tile The tile entity to mark for saving.
     * @param s    The supplier of the integer value.
     * @param c    The consumer of the integer value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forInteger(TileEntity tile, IntSupplier s, IntConsumer c) {
        return new SyncedIntReferenceHolder() {

            @Override
            public int get() {
                return s.getAsInt();
            }

            @Override
            public void set(int value) {
                c.accept(value);
                tile.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link IIntArray} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with potential higher values than 2^15-1.
     * For smaller values use {@link #forInteger(TileEntity, IntSupplier, IntConsumer)}.
     *
     * @param tile The tile entity to mark for saving.
     * @param s    The supplier of the integer value.
     * @param c    The consumer of the integer value.
     * @return The new {@link IIntArray}.
     */
    public static IntReferenceHolder[] forIntegerSplit(TileEntity tile, IntSupplier s, IntConsumer c) {
        return intArrayToReferenceHolder(new IIntArray() {
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
                tile.setChanged();
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    private static SyncedIntReferenceHolder[] intArrayToReferenceHolder(IIntArray intArray) {
        return IntStream
            .range(0, intArray.getCount())
            .mapToObj(i -> SyncedIntReferenceHolder.of(intArray, i))
            .toArray(SyncedIntReferenceHolder[]::new);
    }

    private abstract static class SyncedIntReferenceHolder extends IntReferenceHolder {

        private boolean initSync = true;

        private static SyncedIntReferenceHolder of(IIntArray data, int index) {
            return new SyncedIntReferenceHolder() {
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
