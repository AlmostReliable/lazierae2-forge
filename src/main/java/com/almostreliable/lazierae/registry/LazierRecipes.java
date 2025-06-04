package com.almostreliable.lazierae.registry;

import com.almostreliable.lazierae.Constants;
import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.recipe.CrystalInjectorRecipe;
import com.almostreliable.lazierae.recipe.ProcessingMachineRecipe;
import com.almostreliable.lib.hook.RecipeManagerHooks;
import com.almostreliable.lib.registry.RecipeEntry;
import com.almostreliable.lib.registry.RecipeRegistration;

public final class LazierRecipes {

    private static final RecipeRegistration REGISTRY = LazierAE2.MANAGER.recipes();

    //@formatter:off

    public static final RecipeEntry<CrystalInjectorRecipe> INJECTOR = REGISTRY.register(Constants.INJECTOR_ID, CrystalInjectorRecipe.Serializer::new);
    public static final RecipeEntry<ProcessingMachineRecipe> ETCHER = REGISTRY.register(Constants.ETCHER_ID, ProcessingMachineRecipe.Serializer::new);
    public static final RecipeEntry<ProcessingMachineRecipe> INFUSER = REGISTRY.register(Constants.INFUSER_ID, ProcessingMachineRecipe.Serializer::new);

    //@formatter:on

    private LazierRecipes() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing recipes");

        RecipeManagerHooks.onRecipeReloadBefore(LazierRecipes::onRecipeReload);
    }

    private static void onRecipeReload() {
        CrystalInjectorRecipe.clearCaches();
    }
}
