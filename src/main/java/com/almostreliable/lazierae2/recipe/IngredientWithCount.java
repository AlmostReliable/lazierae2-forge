package com.almostreliable.lazierae2.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import static com.almostreliable.lazierae2.core.Constants.Recipe.COUNT;
import static com.almostreliable.lazierae2.core.Constants.Recipe.INPUT;

public record IngredientWithCount(Ingredient ingredient, int count) {

    public static IngredientWithCount fromJson(JsonObject json) {
        var ingredient = Ingredient.fromJson(json);
        var count = GsonHelper.getAsInt(json, COUNT, 1);
        return new IngredientWithCount(ingredient, count);
    }

    public static IngredientWithCount fromNetwork(FriendlyByteBuf buffer) {
        var ingredient = Ingredient.fromNetwork(buffer);
        var count = buffer.readVarInt();
        return new IngredientWithCount(ingredient, count);
    }

    public void toJson(JsonObject json) {
        json.add(INPUT, toJson());
    }

    public void toJson(JsonArray json) {
        json.add(toJson());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        ingredient.toNetwork(buffer);
        buffer.writeVarInt(count);
    }

    private JsonElement toJson() {
        var input = ingredient.toJson();
        if (count > 1) input.getAsJsonObject().addProperty(COUNT, count);
        return input;
    }
}
