package com.almostreliable.lazierae2.recipe.property;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record RecipeOutputResource(ResourceLocation id, int count) implements IRecipeOutputProvider {

    @Override
    public ItemStack stack() {
        throw new UnsupportedOperationException("RecipeOutputResource does not support stack()");
    }
}
