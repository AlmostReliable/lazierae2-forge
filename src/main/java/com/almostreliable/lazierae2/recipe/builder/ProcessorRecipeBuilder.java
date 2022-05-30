package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
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
import java.util.Objects;
import java.util.function.Consumer;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class ProcessorRecipeBuilder {

    private final ItemStack output;
    private final ProcessorType recipeType;
    private final List<ICondition> conditions = new ArrayList<>();
    private final NonNullList<Ingredient> inputs = NonNullList.create();
    private int processTime;
    private int energyCost;

    private ProcessorRecipeBuilder(ProcessorType recipeType, ItemLike output, int outputCount) {
        this.recipeType = recipeType;
        this.output = new ItemStack(output, outputCount);
    }

    public static ProcessorRecipeBuilder aggregator(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.AGGREGATOR, output, outputCount);
    }

    public static ProcessorRecipeBuilder aggregator(ItemLike output) {
        return aggregator(output, 1);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.ETCHER, output, outputCount);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output) {
        return etcher(output, 1);
    }

    public static ProcessorRecipeBuilder grinder(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.GRINDER, output, outputCount);
    }

    public static ProcessorRecipeBuilder grinder(ItemLike output) {
        return grinder(output, 1);
    }

    public static ProcessorRecipeBuilder infuser(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.INFUSER, output, outputCount);
    }

    public static ProcessorRecipeBuilder infuser(ItemLike output) {
        return infuser(output, 1);
    }

    public ProcessorRecipeBuilder modLoaded(String... modIds) {
        for (var id : modIds) {
            conditions.add(new ModLoadedCondition(id));
        }
        return this;
    }

    public ProcessorRecipeBuilder input(Ingredient... inputs) {
        Collections.addAll(this.inputs, inputs);
        return this;
    }

    public ProcessorRecipeBuilder input(ItemLike input) {
        return input(Ingredient.of(input));
    }

    public ProcessorRecipeBuilder input(TagKey<Item> input) {
        return input(Ingredient.of(input));
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
        var outputId = output.getItem().getRegistryName();
        var modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        var recipeId = new ResourceLocation(modID, f("{}/{}{}", recipeType.getId(), outputId.getPath(), suffix));
        consumer.accept(new FinishedProcessorRecipe(build(recipeId)));
    }

    public void build(Consumer<? super FinishedRecipe> consumer) {
        build(consumer, "");
    }

    public ProcessorRecipe build(ResourceLocation recipeId) {
        var recipe = recipeType.getRecipeFactory().apply(recipeId, recipeType);
        recipe.getInputs().addAll(inputs);
        recipe.setOutput(output);
        recipe.setProcessTime(processTime);
        recipe.setEnergyCost(energyCost);
        recipe.setConditions(conditions);
        recipe.validate();
        return recipe;
    }

    public ItemStack getOutput() {
        return output;
    }
}
