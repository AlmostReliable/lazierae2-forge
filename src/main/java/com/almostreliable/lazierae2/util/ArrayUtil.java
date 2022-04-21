package com.almostreliable.lazierae2.util;

import java.util.function.Supplier;

public final class ArrayUtil {

    private ArrayUtil() {}

    public static <T> void fillUnique(T[] array, Supplier<? extends T> factory) {
        for (int i = 0, len = array.length; i < len; i++) {
            array[i] = factory.get();
        }
    }
}
