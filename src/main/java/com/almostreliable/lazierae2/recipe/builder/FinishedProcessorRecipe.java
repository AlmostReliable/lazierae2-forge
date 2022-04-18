package com.almostreliable.lazierae2.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class FinishedProcessorRecipe implements FinishedRecipe {

    private final ProcessorRecipeBuilder builder;
    private final ResourceLocation id;

    FinishedProcessorRecipe(ProcessorRecipeBuilder builder, ResourceLocation id) {
        this.builder = builder;
        this.id = id;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        json.addProperty(RECIPE_PROCESS_TIME, builder.processingTime);
        json.addProperty(RECIPE_ENERGY_COST, builder.energyCost);
        var output = new JsonObject();
        output.addProperty(RECIPE_ITEM, Objects.requireNonNull(
            builder.getOutput().getItem().getRegistryName(),
            () -> f("Output in {}-recipe was not defined!", builder.getProcessorId())
        ).toString());
        if (builder.getOutput().getCount() > 1) output.addProperty(RECIPE_AMOUNT, builder.getOutput().getCount());
        json.add(RECIPE_OUTPUT, output);
        var inputs = new JsonArray();
        builder.inputs.forEach(input -> inputs.add(input.toJson()));
        json.add(RECIPE_INPUT, inputs);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getType() {
        return builder.getRecipeType().getRecipeSerializer().get();
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
