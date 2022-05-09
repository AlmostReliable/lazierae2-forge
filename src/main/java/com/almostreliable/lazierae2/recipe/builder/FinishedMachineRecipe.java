package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.almostreliable.lazierae2.core.Constants.*;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class FinishedMachineRecipe implements IFinishedRecipe {

    private final MachineRecipe recipe;

    FinishedMachineRecipe(MachineRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        json.addProperty(RECIPE_PROCESS_TIME, recipe.getProcessTime());
        json.addProperty(RECIPE_ENERGY_COST, recipe.getEnergyCost());
        JsonObject output = new JsonObject();
        output.addProperty(RECIPE_ITEM, Objects.requireNonNull(
            recipe.getResultItem().getItem().getRegistryName(),
            () -> f("Output in {}-recipe was not defined!", recipe.getType())
        ).toString());
        if (recipe.getResultItem().getCount() > 1) output.addProperty(RECIPE_COUNT, recipe.getResultItem().getCount());
        json.add(RECIPE_OUTPUT, output);
        JsonArray inputs = new JsonArray();
        recipe.getInputs().forEach(input -> inputs.add(input.toJson()));
        json.add(RECIPE_INPUT, inputs);
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public IRecipeSerializer<?> getType() {
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
