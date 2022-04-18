package com.almostreliable.lazierae2.util;

import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import static com.almostreliable.lazierae2.core.Constants.*;

public final class RecipeUtil {

    private RecipeUtil() {}

    /**
     * Deserializes recipe information from a JSON.
     *
     * @param json   the json to read
     * @param recipe the recipe to apply the information to
     * @return the recipe with the deserialized information
     */
    public static ProcessorRecipe fromJSON(JsonObject json, ProcessorRecipe recipe) {
        recipe.setProcessTime(GsonHelper.getAsInt(json, RECIPE_PROCESS_TIME, 200));
        recipe.setEnergyCost(GsonHelper.getAsInt(json, RECIPE_ENERGY_COST, 1_000));
        recipe.setOutput(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RECIPE_OUTPUT)));
        GsonHelper.getAsJsonArray(json, RECIPE_INPUT).forEach(jsonInput -> {
            var input = deserializeIngredient(jsonInput);
            recipe.getInputs().add(input);
        });

        return recipe;
    }

    /**
     * Deserializes recipe information from a packet buffer.
     *
     * @param buffer the packet buffer to read
     * @param recipe the recipe to apply the information to
     * @return the recipe with the deserialized information
     */
    public static ProcessorRecipe fromNetwork(FriendlyByteBuf buffer, ProcessorRecipe recipe) {
        recipe.setProcessTime(buffer.readInt());
        recipe.setEnergyCost(buffer.readInt());
        recipe.setOutput(buffer.readItem());
        recipe.getInputs().clear();
        int size = buffer.readByte();
        for (var i = 0; i < size; i++) {
            recipe.getInputs().add(Ingredient.fromNetwork(buffer));
        }

        return recipe;
    }

    /**
     * Serializes recipe information to a packet buffer.
     *
     * @param buffer the packet buffer to write to
     * @param recipe the recipe to get the information from
     */
    public static void toNetwork(FriendlyByteBuf buffer, ProcessorRecipe recipe) {
        buffer.writeInt(recipe.getProcessTime());
        buffer.writeInt(recipe.getEnergyCost());
        buffer.writeItem(recipe.getResultItem());
        buffer.writeByte(recipe.getInputs().size());
        recipe.getInputs().forEach(input -> input.toNetwork(buffer));
    }

    /**
     * Utility function to get an ingredient from
     * a given Json Element.
     *
     * @param element the json element to get the ingredient from
     * @return the parsed deserialized ingredient
     */
    private static Ingredient deserializeIngredient(JsonElement element) {
        if (element.isJsonObject()) {
            var json = element.getAsJsonObject();
            if (json.has(RECIPE_INPUT)) return Ingredient.fromJson(json.get(RECIPE_INPUT));
        }
        return Ingredient.fromJson(element);
    }
}
