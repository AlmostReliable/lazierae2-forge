package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class IntegerDataHandler extends GenericDataHandler<Integer> {

    public IntegerDataHandler(Supplier<Integer> getter, Consumer<? super Integer> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Integer value) {
        buffer.writeInt(value);
    }

    @Override
    protected Integer handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readInt();
    }
}
