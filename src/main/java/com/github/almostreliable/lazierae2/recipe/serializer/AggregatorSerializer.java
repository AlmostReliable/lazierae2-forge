package com.github.almostreliable.lazierae2.recipe.serializer;

import com.github.almostreliable.lazierae2.recipe.type.AggregatorRecipe;
import com.github.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class AggregatorSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AggregatorRecipe> {

    @Override
    public AggregatorRecipe fromJson(ResourceLocation id, JsonObject json) {
        AggregatorRecipe recipe = new AggregatorRecipe(id);
        return (AggregatorRecipe) RecipeUtil.fromJSON(json, recipe);
    }

    @Nullable
    @Override
    public AggregatorRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
        AggregatorRecipe recipe = new AggregatorRecipe(id);
        return (AggregatorRecipe) RecipeUtil.fromNetwork(buffer, recipe);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, AggregatorRecipe recipe) {
        RecipeUtil.toNetwork(buffer, recipe);
    }
}
