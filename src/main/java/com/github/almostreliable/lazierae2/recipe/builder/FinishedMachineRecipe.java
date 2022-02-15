package com.github.almostreliable.lazierae2.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public abstract class FinishedMachineRecipe<B extends MachineRecipeBuilder> implements IFinishedRecipe {

    private final B builder;
    private final ResourceLocation recipeId;
    private final String type;

    FinishedMachineRecipe(B builder, ResourceLocation recipeId, String type) {
        this.builder = builder;
        this.recipeId = recipeId;
        this.type = type;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        json.addProperty(RECIPE_PROCESS_TIME, builder.processingTime);
        json.addProperty(RECIPE_ENERGY_COST, builder.energyCost);
        JsonObject output = new JsonObject();
        output.addProperty(RECIPE_ITEM, Objects
            .requireNonNull(builder.output.getItem().getRegistryName(),
                () -> "Output in " + type + "-recipe was not defined!"
            )
            .toString());
        if (builder.output.getCount() > 1) output.addProperty(RECIPE_AMOUNT, builder.output.getCount());
        json.add(RECIPE_OUTPUT, output);
        JsonArray inputs = new JsonArray();
        builder.inputs.forEach(input -> inputs.add(input.toJson()));
        json.add(RECIPE_INPUT, inputs);
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
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
