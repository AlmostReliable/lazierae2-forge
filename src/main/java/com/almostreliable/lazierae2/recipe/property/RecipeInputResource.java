package com.almostreliable.lazierae2.recipe.property;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public record RecipeInputResource(ResourceLocation id, int count) implements IRecipeInputProvider {

    @Override
    public void toJson(JsonObject json) {
        json.add(INPUT, toJson());
    }

    @Override
    public void toJson(JsonArray json) {
        json.add(toJson());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        var item = Registry.ITEM.getOptional(id).orElseThrow();
        Ingredient.of(item).toNetwork(buffer);
        buffer.writeVarInt(count);
    }

    @Override
    public Ingredient ingredient() {
        throw new UnsupportedOperationException("RecipeInputResource does not support ingredient()");
    }

    private JsonElement toJson() {
        var input = new JsonObject();
        input.addProperty(ITEM, id.toString());
        if (count > 1) input.getAsJsonObject().addProperty(COUNT, count);
        return input;
    }
}
