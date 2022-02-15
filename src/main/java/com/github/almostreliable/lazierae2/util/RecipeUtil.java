package com.github.almostreliable.lazierae2.util;

import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public final class RecipeUtil {

    private RecipeUtil() {}

    /**
     * Deserializes recipe information from a JSON.
     *
     * @param json   the json to read
     * @param recipe the recipe to apply the information to
     * @return the recipe with the deserialized information
     */
    public static MachineRecipe fromJSON(JsonObject json, MachineRecipe recipe) {
        recipe.setProcessTime(JSONUtils.getAsInt(json, RECIPE_PROCESS_TIME, 200));
        recipe.setEnergyCost(JSONUtils.getAsInt(json, RECIPE_ENERGY_COST, 1_000));
        recipe.setOutput(ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, RECIPE_OUTPUT)));
        JSONUtils.getAsJsonArray(json, RECIPE_INPUT).forEach(jsonInput -> {
            Ingredient input = deserializeIngredient(jsonInput);
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
    public static MachineRecipe fromNetwork(PacketBuffer buffer, MachineRecipe recipe) {
        recipe.setProcessTime(buffer.readInt());
        recipe.setEnergyCost(buffer.readInt());
        recipe.setOutput(buffer.readItem());
        recipe.getInputs().clear();
        int size = buffer.readByte();
        for (int i = 0; i < size; i++) {
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
    public static void toNetwork(PacketBuffer buffer, MachineRecipe recipe) {
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
            JsonObject json = element.getAsJsonObject();
            if (json.has(RECIPE_INPUT)) return Ingredient.fromJson(json.get(RECIPE_INPUT));
        }
        return Ingredient.fromJson(element);
    }
}
