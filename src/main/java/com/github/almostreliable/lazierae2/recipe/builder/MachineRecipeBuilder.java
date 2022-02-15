package com.github.almostreliable.lazierae2.recipe.builder;

import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Consumer;

import static com.github.almostreliable.lazierae2.core.Constants.MOD_ID;

@SuppressWarnings("ClassReferencesSubclass")
public abstract class MachineRecipeBuilder {

    protected final ItemStack output;
    NonNullList<Ingredient> inputs = NonNullList.create();
    int processingTime;
    int energyCost;

    MachineRecipeBuilder(IItemProvider output, int outputCount) {
        this.output = new ItemStack(output, outputCount);
    }

    public static AggregatorRecipeBuilder aggregator(IItemProvider output, int outputCount) {
        return new AggregatorRecipeBuilder(output, outputCount);
    }

    public static AggregatorRecipeBuilder aggregator(IItemProvider output) {
        return new AggregatorRecipeBuilder(output, 1);
    }

    public static CentrifugeRecipeBuilder centrifuge(IItemProvider output, int outputCount) {
        return new CentrifugeRecipeBuilder(output, outputCount);
    }

    public static CentrifugeRecipeBuilder centrifuge(IItemProvider output) {
        return new CentrifugeRecipeBuilder(output, 1);
    }

    public static EnergizerRecipeBuilder energizer(IItemProvider output, int outputCount) {
        return new EnergizerRecipeBuilder(output, outputCount);
    }

    public static EnergizerRecipeBuilder energizer(IItemProvider output) {
        return new EnergizerRecipeBuilder(output, 1);
    }

    public static EtcherRecipeBuilder etcher(IItemProvider output, int outputCount) {
        return new EtcherRecipeBuilder(output, outputCount);
    }

    public static EtcherRecipeBuilder etcher(IItemProvider output) {
        return new EtcherRecipeBuilder(output, 1);
    }

    public void build(Consumer<? super IFinishedRecipe> consumer) {
        ResourceLocation outputId = output.getItem().getRegistryName();
        String modID = "minecraft".equals(Objects.requireNonNull(outputId).getNamespace()) ? MOD_ID :
            outputId.getNamespace();
        ResourceLocation recipeId = new ResourceLocation(modID, getMachineId() + "/" + outputId.getPath());
        validateProcessingTime();
        validateEnergyCost();
        build(consumer, recipeId);
    }

    protected abstract void validateProcessingTime();

    protected abstract void validateEnergyCost();

    protected abstract void build(Consumer<? super IFinishedRecipe> consumer, ResourceLocation recipeId);

    protected abstract String getMachineId();
}
