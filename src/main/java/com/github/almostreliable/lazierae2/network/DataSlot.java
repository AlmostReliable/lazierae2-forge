package com.github.almostreliable.lazierae2.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntReferenceHolder;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class DataSlot extends IntReferenceHolder {

    private final TileEntity tile;
    private final IntSupplier supplier;
    private final IntConsumer consumer;

    public DataSlot(TileEntity tile, IntSupplier supplier, IntConsumer consumer) {
        this.tile = tile;
        this.supplier = supplier;
        this.consumer = consumer;
    }

    @Override
    public int get() {
        return supplier.getAsInt();
    }

    @Override
    public void set(int value) {
        consumer.accept(value);
        tile.setChanged();
    }
}
