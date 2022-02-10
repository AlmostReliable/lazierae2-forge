package com.github.almostreliable.lazierae2.util;

import appeng.core.Api;
import net.minecraft.item.ItemStack;

public final class GameUtil {

    private GameUtil() {}

    public static boolean isUpgrade(ItemStack stack) {
        return stack.getItem().equals(Api.instance().definitions().materials().cardSpeed().item());
    }
}
