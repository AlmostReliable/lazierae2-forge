package com.github.almostreliable.lazierae2.util;

import net.minecraft.item.ItemStack;

public class GameUtil {
    private GameUtil() {}

    public static boolean isUpgrade(ItemStack stack) {
        // TODO: add AE2 api and implement this
        return false;
        // return stack.getItem() == Api.instance().definitions().materials().cardSpeed().item();
    }
}
