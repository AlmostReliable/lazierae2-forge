package com.github.almostreliable.lazierae2.recipe.serializer;

import com.github.almostreliable.lazierae2.recipe.type.EnergizerRecipe;
import com.github.almostreliable.lazierae2.util.RecipeUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class EnergizerSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EnergizerRecipe> {

    @Override
    public EnergizerRecipe fromJson(ResourceLocation id, JsonObject json) {
        EnergizerRecipe recipe = new EnergizerRecipe(id);
        return (EnergizerRecipe) RecipeUtil.fromJSON(json, recipe);
    }

    @Nullable
    @Override
    public EnergizerRecipe fromNetwork(ResourceLocation id, PacketBuffer buffer) {
        EnergizerRecipe recipe = new EnergizerRecipe(id);
        return (EnergizerRecipe) RecipeUtil.fromNetwork(buffer, recipe);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, EnergizerRecipe recipe) {
        RecipeUtil.toNetwork(buffer, recipe);
    }
}
