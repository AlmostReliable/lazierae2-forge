package com.almostreliable.lazierae2.recipe.builder;

import appeng.core.AppEng;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IRecipeItemProvider;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import com.almostreliable.lazierae2.recipe.RecipeResourceProvider;
import com.almostreliable.lazierae2.recipe.RecipeStackProvider;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class ProcessorRecipeBuilder {

    private final ProcessorType recipeType;
    private final List<ICondition> conditions = new ArrayList<>();
    private final IRecipeItemProvider output;
    private final NonNullList<IngredientWithCount> inputs = NonNullList.create();
    private int processTime;
    private int energyCost;

    private ProcessorRecipeBuilder(ProcessorType recipeType, ItemLike output, int outputCount) {
        this.recipeType = recipeType;
        this.output = new RecipeStackProvider(new ItemStack(output, outputCount));
    }

    private ProcessorRecipeBuilder(ProcessorType recipeType, String output, int outputCount) {
        this.recipeType = recipeType;
        var outputSplit = output.split(":");
        var outputId = new ResourceLocation(outputSplit[0], outputSplit[1]);
        this.output = new RecipeResourceProvider(outputId, outputCount);
    }

    public static ProcessorRecipeBuilder aggregator(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.AGGREGATOR, output, outputCount);
    }

    public static ProcessorRecipeBuilder aggregator(ItemLike output) {
        return aggregator(output, 1);
    }

    public static ProcessorRecipeBuilder aggregator(String output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.AGGREGATOR, output, outputCount);
    }

    public static ProcessorRecipeBuilder aggregator(String output) {
        return aggregator(output, 1);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.ETCHER, output, outputCount);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output) {
        return etcher(output, 1);
    }

    public static ProcessorRecipeBuilder etcher(String output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.ETCHER, output, outputCount);
    }

    public static ProcessorRecipeBuilder etcher(String output) {
        return etcher(output, 1);
    }

    public static ProcessorRecipeBuilder grinder(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.GRINDER, output, outputCount);
    }

    public static ProcessorRecipeBuilder grinder(ItemLike output) {
        return grinder(output, 1);
    }

    public static ProcessorRecipeBuilder grinder(String output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.GRINDER, output, outputCount);
    }

    public static ProcessorRecipeBuilder grinder(String output) {
        return grinder(output, 1);
    }

    public static ProcessorRecipeBuilder infuser(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.INFUSER, output, outputCount);
    }

    public static ProcessorRecipeBuilder infuser(ItemLike output) {
        return infuser(output, 1);
    }

    public static ProcessorRecipeBuilder infuser(String output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.INFUSER, output, outputCount);
    }

    public static ProcessorRecipeBuilder infuser(String output) {
        return infuser(output, 1);
    }

    public ProcessorRecipeBuilder input(IngredientWithCount... inputs) {
        Collections.addAll(this.inputs, inputs);
        return this;
    }

    public ProcessorRecipeBuilder input(Ingredient input) {
        inputs.add(new IngredientWithCount(input, 1));
        return this;
    }

    public ProcessorRecipeBuilder input(Ingredient input, int count) {
        inputs.add(new IngredientWithCount(input, count));
        return this;
    }

    public ProcessorRecipeBuilder input(ItemLike input) {
        return input(Ingredient.of(input));
    }

    public ProcessorRecipeBuilder input(ItemLike input, int count) {
        return input(Ingredient.of(input), count);
    }

    public ProcessorRecipeBuilder input(TagKey<Item> input) {
        return input(Ingredient.of(input));
    }

    public ProcessorRecipeBuilder input(TagKey<Item> input, int count) {
        return input(Ingredient.of(input), count);
    }

    /**
     * Sets the processing time of the recipe. 20 ticks = 1 second.
     * <p>
     * Will fall back to a default value from the config if not set.
     *
     * @param ticks The processing time of the recipe.
     * @return The builder instance.
     */
    public ProcessorRecipeBuilder processingTime(int ticks) {
        processTime = ticks;
        return this;
    }

    /**
     * Sets the energy cost of the recipe.
     * <p>
     * This is the amount of energy required to process the whole recipe, not the cost per tick.
     * <p>
     * Will fall back to a default value from the config if not set.
     *
     * @param energy The energy cost of the recipe.
     * @return The builder instance.
     */
    public ProcessorRecipeBuilder energyCost(int energy) {
        energyCost = energy;
        return this;
    }

    public void build(Consumer<? super FinishedRecipe> consumer, String suffix) {
        var namespace = output.id().getNamespace().replace("minecraft", MOD_ID);
        var outputId = output.id().getPath();
        var path = f("{}/{}{}", recipeType.getId(), outputId, suffix);
        if (!namespace.equals(MOD_ID)) {
            if (!namespace.equals(AppEng.MOD_ID)) modLoaded(namespace);
            path = f("compat/{}/{}", namespace, path);
        }
        consumer.accept(new FinishedProcessorRecipe(build(TextUtil.getRL(path))));
    }

    public void build(Consumer<? super FinishedRecipe> consumer) {
        build(consumer, "");
    }

    public ProcessorRecipe build(ResourceLocation recipeId) {
        return recipeType.getRecipeFactory()
            .create(recipeId, recipeType, conditions, output, inputs, processTime, energyCost);
    }

    private void modLoaded(String... modIds) {
        for (var id : modIds) {
            conditions.add(new ModLoadedCondition(id));
        }
    }
}
