package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LongDataHandler extends GenericDataHandler<Long> {

    public LongDataHandler(Supplier<Long> getter, Consumer<? super Long> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Long value) {
        buffer.writeLong(value);
    }

    @Override
    protected Long handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readLong();
    }
}
