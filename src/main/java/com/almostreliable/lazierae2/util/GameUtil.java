package com.almostreliable.lazierae2.util;

import appeng.core.definitions.AEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Objects;

public final class GameUtil {

    private GameUtil() {}

    public static boolean isValidUpgrade(ItemStack stack) {
        return stack.getItem().equals(AEItems.SPEED_CARD.asItem());
    }

    public static String getIdFromItem(Item item) {
        return Objects.requireNonNull(item.getRegistryName()).getPath();
    }

    public static String getIdFroBlock(Block block) {
        return Objects.requireNonNull(block.getRegistryName()).getPath();
    }

    public static RecipeManager getRecipeManager(@Nullable Level level) {
        if (level != null && level.getServer() != null) return level.getServer().getRecipeManager();
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        }
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.getRecipeManager();
    }
}
