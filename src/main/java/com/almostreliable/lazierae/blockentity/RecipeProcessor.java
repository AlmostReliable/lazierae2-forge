package com.almostreliable.lazierae.blockentity;

import net.minecraft.world.item.crafting.Recipe;

import appeng.api.networking.ticking.TickRateModulation;

public interface RecipeProcessor<R extends Recipe<?>> {

    default TickRateModulation handleTickingRequest() {
        if (getRecipeContainer().isPresent()) return processRecipe();
        return findAndProcessRecipe();
    }

    TickRateModulation findAndProcessRecipe();

    TickRateModulation processRecipe();

    default void resetRecipe() {
        getRecipeContainer().reset();
    }

    RecipeProcessContainer<R> getRecipeContainer();
}
