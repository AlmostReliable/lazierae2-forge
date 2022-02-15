package com.github.almostreliable.lazierae2.recipe.serializer;

import com.github.almostreliable.lazierae2.recipe.type.EtcherRecipe;
import com.github.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class EtcherSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EtcherRecipe> {

    @Override
    public EtcherRecipe fromJson(ResourceLocation id, JsonObject json) {
        EtcherRecipe recipe = new EtcherRecipe(id);
        return (EtcherRecipe) RecipeUtil.fromJSON(json, recipe);
    }

    @Nullable
    @Override
    public EtcherRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
        EtcherRecipe recipe = new EtcherRecipe(id);
        return (EtcherRecipe) RecipeUtil.fromNetwork(buffer, recipe);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, EtcherRecipe recipe) {
        RecipeUtil.toNetwork(buffer, recipe);
    }
}
