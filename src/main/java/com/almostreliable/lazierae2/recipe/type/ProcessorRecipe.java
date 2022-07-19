package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.property.IRecipeInputProvider;
import com.almostreliable.lazierae2.recipe.property.IRecipeOutputProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class ProcessorRecipe implements Recipe<Container> {

    private final ResourceLocation recipeId;
    private final ProcessorType processorType;
    private final List<ICondition> conditions;
    private final IRecipeOutputProvider output;
    private final NonNullList<IRecipeInputProvider> inputs;
    private final int processTime;
    private final int energyCost;

    ProcessorRecipe(
        ResourceLocation recipeId, ProcessorType processorType, List<ICondition> conditions,
        IRecipeOutputProvider output,
        NonNullList<IRecipeInputProvider> inputs, int processTime, int energyCost
    ) {
        this.recipeId = recipeId;
        this.processorType = processorType;
        this.conditions = conditions;
        this.output = output;
        this.inputs = inputs;
        this.processTime = processTime == 0 ? processorType.getBaseProcessTime() : processTime;
        this.energyCost = energyCost == 0 ? processorType.getBaseEnergyCost() : energyCost;
        validateInputs();
    }

    @Override
    public ItemStack assemble(Container inv) {
        return output.stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return output.stack();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return processorType.getRecipeSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return processorType;
    }

    private void validateInputs() {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException(f(
                "No inputs for recipe type '{}' with output '{}'!",
                processorType.getId(),
                output.toString()
            ));
        }
        if (inputs.size() > processorType.getInputSlots()) {
            throw new IllegalArgumentException(f(
                "Too many inputs for recipe type '{}' with output '{}'!",
                processorType.getId(),
                output.toString()
            ));
        }
    }

    public IRecipeOutputProvider getOutput() {
        return output;
    }

    public List<ICondition> getConditions() {
        return conditions;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public NonNullList<IRecipeInputProvider> getInputs() {
        return inputs;
    }
}
