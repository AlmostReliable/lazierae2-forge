package com.almostreliable.lazierae2.util;

import appeng.core.Api;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public final class GameUtil {

    private GameUtil() {}

    public static boolean isValidUpgrade(ItemStack stack) {
        return stack.getItem().equals(Api.instance().definitions().materials().cardSpeed().item());
    }

    public static RecipeManager getRecipeManager(@Nullable World world) {
        if (world != null && world.getServer() != null) return world.getServer().getRecipeManager();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager();
    }
}
