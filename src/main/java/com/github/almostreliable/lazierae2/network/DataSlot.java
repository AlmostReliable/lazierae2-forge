package com.github.almostreliable.lazierae2.network;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntReferenceHolder;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class DataSlot {

    private DataSlot() {}

    /**
     * Utility method to create a new {@link IntReferenceHolder} for integer values without
     * the need to have a clunky anonymous class.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with the maximum value of 2^15-1.
     * For larger values, use {@link #forIntegerLower(TileEntity, IntSupplier, IntConsumer)} and
     * {@link #forIntegerUpper(TileEntity, IntSupplier, IntConsumer)}.
     *
     * @param tile     The tile entity to mark for saving.
     * @param supplier The supplier of the integer value.
     * @param consumer The consumer of the integer value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forInteger(TileEntity tile, IntSupplier supplier, IntConsumer consumer) {
        return new IntReferenceHolder() {

            @Override
            public int get() {
                return supplier.getAsInt();
            }

            @Override
            public void set(int value) {
                consumer.accept(value);
                tile.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link IntReferenceHolder} for integer values without
     * the need to have a clunky anonymous class.
     * This method has to be used with {@link #forIntegerUpper(TileEntity, IntSupplier, IntConsumer)}.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with potential higher values than 2^15-1.
     * For smaller values use {@link #forInteger(TileEntity, IntSupplier, IntConsumer)}.
     *
     * @param tile     The tile entity to mark for saving.
     * @param supplier The supplier of the integer value.
     * @param consumer The consumer of the integer value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forIntegerLower(TileEntity tile, IntSupplier supplier, IntConsumer consumer) {
        return new IntReferenceHolder() {

            @Override
            public int get() {
                return (short) supplier.getAsInt();
            }

            @Override
            public void set(int value) {
                int currentValue = supplier.getAsInt() & 0xFFFF_0000;
                consumer.accept(currentValue + (value & 0xFFFF));
                tile.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link IntReferenceHolder} for integer values without
     * the need to have a clunky anonymous class.
     * This method has to be used with {@link #forIntegerLower(TileEntity, IntSupplier, IntConsumer)}.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     * <p>
     * This method should only be used for integer values with potential higher values than 2^15-1.
     * For smaller values use {@link #forInteger(TileEntity, IntSupplier, IntConsumer)}.
     *
     * @param tile     The tile entity to mark for saving.
     * @param supplier The supplier of the integer value.
     * @param consumer The consumer of the integer value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forIntegerUpper(TileEntity tile, IntSupplier supplier, IntConsumer consumer) {
        return new IntReferenceHolder() {

            @Override
            public int get() {
                return (short) (supplier.getAsInt() >> 16);
            }

            @Override
            public void set(int value) {
                int currentValue = supplier.getAsInt() & 0x0000_FFFF;
                consumer.accept(currentValue | (value << 16));
                tile.setChanged();
            }
        };
    }

    /**
     * Utility method to create a new {@link IntReferenceHolder} for boolean values without
     * the need to have a clunky anonymous class and conversion.
     * <p>
     * Marks the tile entity automatically for saving when changing values.
     *
     * @param tile     The tile entity to mark for saving.
     * @param supplier The supplier of the boolean value.
     * @param consumer The consumer of the boolean value.
     * @return The new {@link IntReferenceHolder}.
     */
    public static IntReferenceHolder forBoolean(TileEntity tile, BooleanSupplier supplier, BooleanConsumer consumer) {
        return new IntReferenceHolder() {

            @Override
            public int get() {
                return supplier.getAsBoolean() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                consumer.accept(value == 1);
                tile.setChanged();
            }
        };
    }
}
