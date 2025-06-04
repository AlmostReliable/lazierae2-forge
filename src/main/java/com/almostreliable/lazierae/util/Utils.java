package com.almostreliable.lazierae.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.almostreliable.lazierae.LazierAE2Constants;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.List;

public final class Utils {

    private Utils() {
    }

    public static int fillColorAlpha(ChatFormatting color) {
        // noinspection ConstantConditions
        return 0xFF << 3 * 8 | color.getColor();
    }

    public static MutableComponent translate(String type, String key, Object... args) {
        return Component.translatable(getTranslationKey(type, key), args);
    }

    public static String translateAsString(String type, String key) {
        return translate(type, key).getString();
    }

    public static void addShiftInfoTooltip(List<Component> tooltip) {
        tooltip.add(Component.literal("» ").withStyle(ChatFormatting.AQUA).append(translate(
            "tooltip",
            "shift_for_more",
            InputConstants.getKey("key.keyboard.left.shift").getDisplayName()
        ).withStyle(ChatFormatting.GRAY)));
    }

    public static <T> T cast(Object o, Class<T> clazz) {
        return clazz.cast(o);
    }

    private static String getTranslationKey(String type, String key) {
        return String.format("%s.%s.%s", type.toLowerCase(), LazierAE2Constants.MOD_ID, key);
    }

    public static int[] ArrayFromTo(int from, int to) {
        int[] array = new int[to - from + 1];
        for (int i = from; i <= to; i++) {
            array[i - from] = i;
        }
        return array;
    }
}
