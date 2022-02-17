package com.github.almostreliable.lazierae2.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class FinishedMachineRecipe implements IFinishedRecipe {

    private final MachineRecipeBuilder builder;
    private final ResourceLocation recipeId;

    FinishedMachineRecipe(MachineRecipeBuilder builder, ResourceLocation recipeId) {
        this.builder = builder;
        this.recipeId = recipeId;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        json.addProperty(RECIPE_PROCESS_TIME, builder.processingTime);
        json.addProperty(RECIPE_ENERGY_COST, builder.energyCost);
        JsonObject output = new JsonObject();
        output.addProperty(RECIPE_ITEM, Objects.requireNonNull(
            builder.getOutput().getItem().getRegistryName(),
            () -> "Output in " + builder.getMachineId() + "-recipe was not defined!"
        ).toString());
        if (builder.getOutput().getCount() > 1) output.addProperty(RECIPE_AMOUNT, builder.getOutput().getCount());
        json.add(RECIPE_OUTPUT, output);
        JsonArray inputs = new JsonArray();
        builder.inputs.forEach(input -> inputs.add(input.toJson()));
        json.add(RECIPE_INPUT, inputs);
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public IRecipeSerializer<?> getType() {
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
