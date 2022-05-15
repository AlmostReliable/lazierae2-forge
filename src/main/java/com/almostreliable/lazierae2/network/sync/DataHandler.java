package com.almostreliable.lazierae2.network.sync;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DataHandler<T> {

    protected final Supplier<? extends T> getter;
    protected T oldValue;
    private Consumer<? super T> setter;

    protected DataHandler(Supplier<? extends T> getter) {
        this.getter = getter;
        oldValue = getter.get();
    }

    protected DataHandler(Supplier<? extends T> getter, Consumer<? super T> setter) {
        this(getter);
        this.setter = setter;
    }

    public void encode(FriendlyByteBuf buffer) {
        var currentValue = getter.get();
        oldValue = currentValue;
        handleEncoding(buffer, currentValue);
    }

    public void decode(FriendlyByteBuf buffer) {
        setter.accept(handleDecoding(buffer));
    }

    protected abstract void handleEncoding(FriendlyByteBuf buffer, T value);

    protected abstract T handleDecoding(FriendlyByteBuf buffer);

    protected boolean hasChanged() {
        return !Objects.equals(oldValue, getter.get());
    }
}
