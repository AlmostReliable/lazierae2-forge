package com.almostreliable.lazierae2.recipe.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

public interface IRecipeInputProvider {

    void toJson(JsonObject json);

    void toJson(JsonArray json);

    void toNetwork(FriendlyByteBuf buffer);

    int count();

    Ingredient ingredient();
}
