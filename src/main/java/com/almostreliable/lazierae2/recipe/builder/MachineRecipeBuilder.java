package com.almostreliable.lazierae2.recipe.builder;

import com.almostreliable.lazierae2.machine.MachineType;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class MachineRecipeBuilder {

    private final ItemStack output;
    private final MachineType recipeType;
    private final NonNullList<Ingredient> inputs = NonNullList.create();
    private int processTime;
    private int energyCost;

    private MachineRecipeBuilder(MachineType recipeType, IItemProvider output, int outputCount) {
        this.recipeType = recipeType;
        this.output = new ItemStack(output, outputCount);
    }

    public static MachineRecipeBuilder aggregator(IItemProvider output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.AGGREGATOR, output, outputCount);
    }

    public static MachineRecipeBuilder aggregator(IItemProvider output) {
        return aggregator(output, 1);
    }

    public static MachineRecipeBuilder centrifuge(IItemProvider output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.CENTRIFUGE, output, outputCount);
    }

    public static MachineRecipeBuilder centrifuge(IItemProvider output) {
        return centrifuge(output, 1);
    }

    public static MachineRecipeBuilder energizer(IItemProvider output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.ENERGIZER, output, outputCount);
    }

    public static MachineRecipeBuilder energizer(IItemProvider output) {
        return energizer(output, 1);
    }

    public static MachineRecipeBuilder etcher(IItemProvider output, int outputCount) {
        return new MachineRecipeBuilder(MachineType.ETCHER, output, outputCount);
    }

    public static MachineRecipeBuilder etcher(IItemProvider output) {
        return etcher(output, 1);
    }

    public MachineRecipeBuilder input(Ingredient... inputs) {
        Collections.addAll(this.inputs, inputs);
        return this;
    }

    public MachineRecipeBuilder input(IItemProvider input) {
        return input(Ingredient.of(input));
    }

    public MachineRecipeBuilder input(ITag<Item> input) {
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
    public MachineRecipeBuilder energyCost(int energy) {
        energyCost = energy;
        return this;
    }

    public void build(Consumer<? super IFinishedRecipe> consumer) {
        ResourceLocation outputId = output.getItem().getRegistryName();
        String modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        ResourceLocation recipeId = new ResourceLocation(modID, f("{}/{}", recipeType.getId(), outputId.getPath()));
        consumer.accept(new FinishedMachineRecipe(build(recipeId)));
    }

    public MachineRecipe build(ResourceLocation recipeId) {
        MachineRecipe recipe = recipeType.getRecipeFactory().apply(recipeId, recipeType);
        recipe.getInputs().addAll(inputs);
        recipe.setOutput(output);
        recipe.setProcessTime(processTime);
        recipe.setEnergyCost(energyCost);
        recipe.validate();
        return recipe;
    }

    public ItemStack getOutput() {
        return output;
    }
}
