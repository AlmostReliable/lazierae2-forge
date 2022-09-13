package com.almostreliable.lazierae2.util;

import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

import static com.almostreliable.lazierae2.BuildConfig.MOD_ID;

public final class GameUtil {

    private static final UUID MOD_UUID = UUID.nameUUIDFromBytes(MOD_ID.getBytes());

    private GameUtil() {}

    public static boolean isValidUpgrade(ItemStack stack) {
        return stack.getItem().equals(AEItems.SPEED_CARD.asItem());
    }

    public static String getIdFromItem(Item item) {
        return Objects.requireNonNull(item.getRegistryName()).getPath();
    }

    public static String getIdFromBlock(Block block) {
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

    public static void sendPlayerMessage(Player player, String translationKey, ChatFormatting color, Object... args) {
        player.sendMessage(TextUtil.translateWithArgs(TRANSLATE_TYPE.MESSAGE, translationKey, color, args), MOD_UUID);
    }
}
