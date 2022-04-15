package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.content.machine.MachineType;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
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

public final class MachineRecipeBuilder {

    private final ItemStack output;
    private final MachineType recipeType;
    NonNullList<Ingredient> inputs = NonNullList.create();
    int processingTime;
    int energyCost;

    private MachineRecipeBuilder(MachineType recipeType, ItemLike output, int outputCount) {
        this.recipeType = recipeType;
        this.output = new ItemStack(output, outputCount);
    }

    public static MachineRecipeBuilder aggregator(ItemLike output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.AGGREGATOR, output, outputCount);
    }

    public static MachineRecipeBuilder aggregator(ItemLike output) {
        return aggregator(output, 1);
    }

    public static MachineRecipeBuilder centrifuge(ItemLike output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.CENTRIFUGE, output, outputCount);
    }

    public static MachineRecipeBuilder centrifuge(ItemLike output) {
        return centrifuge(output, 1);
    }

    public static MachineRecipeBuilder energizer(ItemLike output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.ENERGIZER, output, outputCount);
    }

    public static MachineRecipeBuilder energizer(ItemLike output) {
        return energizer(output, 1);
    }

    public static MachineRecipeBuilder etcher(ItemLike output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.ETCHER, output, outputCount);
    }

    public static MachineRecipeBuilder etcher(ItemLike output) {
        return etcher(output, 1);
    }

    public MachineRecipeBuilder input(Ingredient input) {
        if (inputs.size() < 3) inputs.add(input);
        return this;
    }

    public MachineRecipeBuilder input(Ingredient... inputs) {
        for (var input : inputs) {
            input(input);
        }
        return this;
    }

    public MachineRecipeBuilder input(ItemLike input) {
        return input(Ingredient.of(input));
    }

    public MachineRecipeBuilder input(TagKey<Item> input) {
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
    public MachineRecipeBuilder processingTime(int ticks) {
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
    public MachineRecipeBuilder energyCost(int energy) {
        energyCost = energy;
        return this;
    }

    public void build(Consumer<? super FinishedRecipe> consumer) {
        var outputId = output.getItem().getRegistryName();
        var modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        var recipeId = new ResourceLocation(modID, f("{}/{}", getMachineId(), outputId.getPath()));
        validateProcessingTime();
        validateEnergyCost();
        consumer.accept(new FinishedMachineRecipe(this, recipeId));
    }

    public MachineRecipe build(ResourceLocation id) {
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

    String getMachineId() {
        return recipeType.getId();
    }

    public ItemStack getOutput() {
        return output;
    }

    MachineType getRecipeType() {
        return recipeType;
    }
}
