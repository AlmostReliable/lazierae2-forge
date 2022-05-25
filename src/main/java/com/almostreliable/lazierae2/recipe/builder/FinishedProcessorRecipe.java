package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class FinishedProcessorRecipe implements FinishedRecipe {

    private final ProcessorRecipe recipe;

    public FinishedProcessorRecipe(ProcessorRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        json.addProperty(PROCESS_TIME, recipe.getProcessTime());
        json.addProperty(ENERGY_COST, recipe.getEnergyCost());
        var output = new JsonObject();
        output.addProperty(ITEM, Objects.requireNonNull(
            recipe.getResultItem().getItem().getRegistryName(),
            () -> f("Output in {}-recipe was not defined!", recipe.getType())
        ).toString());
        if (recipe.getResultItem().getCount() > 1) output.addProperty(COUNT, recipe.getResultItem().getCount());
        json.add(OUTPUT, output);
        var inputs = new JsonArray();
        recipe.getInputs().forEach(input -> inputs.add(input.toJson()));
        json.add(INPUT, inputs);
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return recipe.getSerializer();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
