package com.github.almostreliable.lazierae2.recipe.serializer;

import com.github.almostreliable.lazierae2.recipe.type.CentrifugeRecipe;
import com.github.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class CentrifugeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CentrifugeRecipe> {

    @Override
    public CentrifugeRecipe fromJson(ResourceLocation id, JsonObject json) {
        CentrifugeRecipe recipe = new CentrifugeRecipe(id);
        return (CentrifugeRecipe) RecipeUtil.fromJSON(json, recipe);
    }

    @Nullable
    @Override
    public CentrifugeRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
        CentrifugeRecipe recipe = new CentrifugeRecipe(id);
        return (CentrifugeRecipe) RecipeUtil.fromNetwork(buffer, recipe);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, CentrifugeRecipe recipe) {
        RecipeUtil.toNetwork(buffer, recipe);
    }
}
