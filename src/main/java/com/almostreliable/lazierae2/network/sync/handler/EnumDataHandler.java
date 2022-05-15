package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EnumDataHandler<T extends Enum<T>> extends GenericDataHandler<T> {

    private final T[] values;

    @SafeVarargs
    public EnumDataHandler(Supplier<T> getter, Consumer<T> setter, T... values) {
        super(getter, setter);
        this.values = values;
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, @Nullable T value) {
        if (value == null) {
            buffer.writeShort(-1);
        } else {
            buffer.writeShort((short) value.ordinal());
        }
    }

    @Override
    protected T handleDecoding(FriendlyByteBuf buffer) {
        int ordinal = buffer.readShort();
        if (ordinal == -1) {
            return null;
        }
        return values[ordinal];
    }
}
