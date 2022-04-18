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

import java.util.Objects;
import java.util.function.Consumer;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class ProcessorRecipeBuilder {

    private final ItemStack output;
    private final ProcessorType recipeType;
    NonNullList<Ingredient> inputs = NonNullList.create();
    int processingTime;
    int energyCost;

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

    public static ProcessorRecipeBuilder centrifuge(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.CENTRIFUGE, output, outputCount);
    }

    public static ProcessorRecipeBuilder centrifuge(ItemLike output) {
        return centrifuge(output, 1);
    }

    public static ProcessorRecipeBuilder energizer(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.ENERGIZER, output, outputCount);
    }

    public static ProcessorRecipeBuilder energizer(ItemLike output) {
        return energizer(output, 1);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output, int outputCount) {
        return new ProcessorRecipeBuilder(ProcessorType.ETCHER, output, outputCount);
    }

    public static ProcessorRecipeBuilder etcher(ItemLike output) {
        return etcher(output, 1);
    }

    public ProcessorRecipeBuilder input(Ingredient input) {
        if (inputs.size() < 3) inputs.add(input);
        return this;
    }

    public ProcessorRecipeBuilder input(Ingredient... inputs) {
        for (var input : inputs) {
            input(input);
        }
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
        processingTime = ticks;
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

    public void build(Consumer<? super FinishedRecipe> consumer) {
        var outputId = output.getItem().getRegistryName();
        var modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        var recipeId = new ResourceLocation(modID, f("{}/{}", getProcessorId(), outputId.getPath()));
        validateProcessingTime();
        validateEnergyCost();
        consumer.accept(new FinishedProcessorRecipe(this, recipeId));
    }

    public ProcessorRecipe build(ResourceLocation id) {
        validateProcessingTime();
        validateEnergyCost();
        var recipe = recipeType.getRecipeFactory().apply(id, recipeType);
        recipe.setInputs(inputs);
        recipe.setOutput(output);
        recipe.setProcessTime(processingTime);
        recipe.setEnergyCost(energyCost);
        return recipe;
    }

    private void validateProcessingTime() {
        if (processingTime == 0) processingTime = recipeType.getBaseProcessTime();
    }

    private void validateEnergyCost() {
        if (energyCost == 0) energyCost = recipeType.getBaseEnergyCost();
    }

    String getProcessorId() {
        return recipeType.getId();
    }

    public ItemStack getOutput() {
        return output;
    }

    ProcessorType getRecipeType() {
        return recipeType;
    }
}
