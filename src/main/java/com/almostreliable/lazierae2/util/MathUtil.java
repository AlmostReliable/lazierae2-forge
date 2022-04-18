package com.almostreliable.lazierae2.util;

public final class MathUtil {

    private MathUtil() {}

    public static long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}
