package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BooleanDataHandler extends GenericDataHandler<Boolean> {

    public BooleanDataHandler(Supplier<Boolean> getter, Consumer<? super Boolean> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, Boolean value) {
        buffer.writeBoolean(value);
    }

    @Override
    protected Boolean handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readBoolean();
    }
}
