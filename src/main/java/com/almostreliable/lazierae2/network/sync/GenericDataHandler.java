package com.almostreliable.lazierae2.network.sync;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GenericDataHandler<T> implements IDataHandler {

    private final Supplier<? extends T> getter;
    private final Consumer<? super T> setter;
    private T oldValue;

    protected GenericDataHandler(Supplier<? extends T> getter, Consumer<? super T> setter) {
        this.getter = getter;
        this.setter = setter;
        oldValue = getter.get();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        var currentValue = getter.get();
        oldValue = currentValue;
        handleEncoding(buffer, currentValue);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        setter.accept(handleDecoding(buffer));
    }

    @Override
    public boolean hasChanged() {
        return !Objects.equals(oldValue, getter.get());
    }

    protected abstract void handleEncoding(FriendlyByteBuf buffer, T value);

    @Nullable
    protected abstract T handleDecoding(FriendlyByteBuf buffer);
}
