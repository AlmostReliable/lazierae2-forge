package com.almostreliable.lazierae2.network.sync;

import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class DataHandler<T> {

    private final Supplier<? extends T> getter;
    private final Consumer<? super T> setter;
    private T oldValue;

    private DataHandler(Supplier<? extends T> getter, Consumer<? super T> setter) {
        this.getter = getter;
        this.setter = setter;
        oldValue = getter.get();
    }

    @SuppressWarnings({"ChainOfInstanceofChecks", "unchecked", "java:S1452"})
    public static <C> DataHandler<?> create(Class<C> clazz, Supplier<C> getter, Consumer<? super C> setter) {
        if (clazz == String.class) {
            Supplier<String> stringGetter = () -> (String) getter.get();
            Consumer<String> stringSetter = s -> setter.accept((C) s);
            return new StringHandler(stringGetter, stringSetter);
        }
        if (clazz == int.class || clazz == Integer.class) {
            Supplier<Integer> intGetter = () -> (Integer) getter.get();
            Consumer<Integer> intSetter = i -> setter.accept((C) i);
            return new IntegerHandler(intGetter, intSetter);
        }
        if (clazz.isArray() && clazz.getComponentType() == int.class) {
            Supplier<int[]> intArrayGetter = () -> (int[]) getter.get();
            Consumer<int[]> intArraySetter = i -> setter.accept((C) i);
            return new IntArrayHandler(intArrayGetter, intArraySetter);
        }
        if (clazz == long.class || clazz == Long.class) {
            Supplier<Long> longGetter = () -> (Long) getter.get();
            Consumer<Long> longSetter = l -> setter.accept((C) l);
            return new LongHandler(longGetter, longSetter);
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            Supplier<Boolean> booleanGetter = () -> (Boolean) getter.get();
            Consumer<Boolean> booleanSetter = b -> setter.accept((C) b);
            return new BooleanHandler(booleanGetter, booleanSetter);
        }
        throw new IllegalArgumentException("Unsupported type " + clazz.getSimpleName());
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

    boolean hasChanged() {
        return !Objects.equals(oldValue, getter.get());
    }

    private static final class StringHandler extends DataHandler<String> {

        private StringHandler(Supplier<String> getter, Consumer<? super String> setter) {
            super(getter, setter);
        }

        @Override
        protected void handleEncoding(FriendlyByteBuf buffer, String value) {
            buffer.writeUtf(value);
        }

        @Override
        protected String handleDecoding(FriendlyByteBuf buffer) {
            return buffer.readUtf();
        }
    }

    private static final class IntegerHandler extends DataHandler<Integer> {

        private IntegerHandler(Supplier<Integer> getter, Consumer<? super Integer> setter) {
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

    private static final class LongHandler extends DataHandler<Long> {

        private LongHandler(Supplier<Long> getter, Consumer<? super Long> setter) {
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

    private static final class BooleanHandler extends DataHandler<Boolean> {

        private BooleanHandler(Supplier<Boolean> getter, Consumer<? super Boolean> setter) {
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

    private static final class IntArrayHandler extends DataHandler<int[]> {

        private IntArrayHandler(Supplier<int[]> getter, Consumer<int[]> setter) {
            super(getter, setter);
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        @Override
        protected void handleEncoding(FriendlyByteBuf buffer, int[] value) {
            buffer.writeVarIntArray(value);
        }

        @Override
        protected int[] handleDecoding(FriendlyByteBuf buffer) {
            return buffer.readVarIntArray();
        }
    }
}
