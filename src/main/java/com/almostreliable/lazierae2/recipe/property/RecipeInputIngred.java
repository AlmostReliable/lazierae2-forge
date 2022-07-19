package com.almostreliable.lazierae2.recipe.property;

import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

public record RecipeInputIngred(IngredientWithCount ingred) implements IRecipeInputProvider {

    public static RecipeInputIngred fromJson(JsonObject json) {
        return new RecipeInputIngred(IngredientWithCount.fromJson(json));
    }

    public static RecipeInputIngred fromNetwork(FriendlyByteBuf buffer) {
        return new RecipeInputIngred(IngredientWithCount.fromNetwork(buffer));
    }

    @Override
    public void toJson(JsonObject json) {
        ingred.toJson(json);
    }

    @Override
    public void toJson(JsonArray json) {
        ingred.toJson(json);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        ingred.toNetwork(buffer);
    }

    @Override
    public int count() {
        return ingred.count();
    }

    @Override
    public Ingredient ingredient() {
        return ingred.ingredient();
    }
}
