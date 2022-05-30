package com.almostreliable.lazierae2.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record RecipeResourceProvider(ResourceLocation id, int count) implements IRecipeItemProvider {

    @Override
    public ItemStack stack() {
        throw new UnsupportedOperationException("RecipeResourceProvider does not support getStack()");
    }
}
