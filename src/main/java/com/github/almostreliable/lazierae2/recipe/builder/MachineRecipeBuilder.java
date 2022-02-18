package com.github.almostreliable.lazierae2.recipe.builder;

import com.github.almostreliable.lazierae2.core.TypeEnums.MachineType;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Consumer;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

public final class MachineRecipeBuilder {

    private final ItemStack output;
    private final MachineType recipeType;
    NonNullList<Ingredient> inputs = NonNullList.create();
    int processingTime;
    int energyCost;

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

    public MachineRecipeBuilder input(Ingredient input) {
        if (inputs.size() < 3) inputs.add(input);
        return this;
    }

    public MachineRecipeBuilder input(IItemProvider input) {
        return input(Ingredient.of(input));
    }

    public MachineRecipeBuilder input(ITag<Item> input) {
        return input(Ingredient.of(input));
    }

    public MachineRecipeBuilder processingTime(int ticks) {
        processingTime = ticks;
        return this;
    }

    public MachineRecipeBuilder energyCost(int energy) {
        energyCost = energy;
        return this;
    }

    public void build(Consumer<? super IFinishedRecipe> consumer) {
        ResourceLocation outputId = output.getItem().getRegistryName();
        String modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        ResourceLocation recipeId = new ResourceLocation(modID, getMachineId() + "/" + outputId.getPath());
        validateProcessingTime();
        validateEnergyCost();
        consumer.accept(new FinishedMachineRecipe(this, recipeId));
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
