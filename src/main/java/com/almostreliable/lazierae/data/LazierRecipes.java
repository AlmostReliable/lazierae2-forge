package com.almostreliable.lazierae.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.block.Blocks;

import com.almostreliable.lazierae.data.recipe.CrystalInjectorRecipeBuilder;
import com.almostreliable.lib.item.IngredientStack;
import com.almostreliable.lib.util.filter.BlockPredicate;

import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class LazierRecipes extends RecipeProvider {

    LazierRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        CrystalInjectorRecipeBuilder.create(
            IngredientStack.of(Tags.Items.ORES_COAL, 3),
            BlockPredicate.tags(Tags.Blocks.SAND).build(),
            BlockPredicate.blocks(Blocks.GLASS).build()
        ).time(200).energy(100).save(consumer, "glass_to_sand");
    }
}
