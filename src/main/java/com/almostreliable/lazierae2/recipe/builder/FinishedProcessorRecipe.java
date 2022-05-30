package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public class FinishedProcessorRecipe implements FinishedRecipe {

    private final ProcessorRecipe recipe;

    public FinishedProcessorRecipe(ProcessorRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
        if (!recipe.getConditions().isEmpty()) {
            var conditions = new JsonArray();
            recipe.getConditions().forEach(c -> conditions.add(CraftingHelper.serialize(c)));
            json.add(CONDITIONS, conditions);
        }

        recipe.getOutput().toJson(json);

        if (((ProcessorType) recipe.getType()).getInputSlots() == 1) {
            recipe.getInputs().forEach(input -> json.add(INPUT, input.toJson()));
        } else {
            var inputs = new JsonArray();
            recipe.getInputs().forEach(input -> inputs.add(input.toJson()));
            json.add(INPUT, inputs);
        }

        json.addProperty(PROCESS_TIME, recipe.getProcessTime());
        json.addProperty(ENERGY_COST, recipe.getEnergyCost());
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
