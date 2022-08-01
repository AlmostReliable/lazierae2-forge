package com.almostreliable.lazierae2.recipe.property;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.almostreliable.lazierae2.core.Constants.Recipe.*;

public interface IRecipeOutputProvider {

    default void toJson(JsonObject json) {
        var output = new JsonObject();
        output.addProperty(ITEM, id().toString());
        if (count() > 1) output.addProperty(COUNT, count());
        json.add(OUTPUT, output);
    }

    ResourceLocation id();

    int count();

    ItemStack stack();
}
