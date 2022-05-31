package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorInventory;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IRecipeItemProvider;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TripleInputRecipe extends ProcessorRecipe {

    public TripleInputRecipe(
        ResourceLocation recipeId, ProcessorType processorType, List<ICondition> conditions, IRecipeItemProvider output,
        NonNullList<IngredientWithCount> inputs, int processTime, int energyCost
    ) {
        super(recipeId, processorType, conditions, output, inputs, processTime, energyCost);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        if (getInputs().isEmpty()) return false;

        var matchedContainerItems = new Ingredient[inv.getContainerSize() - ProcessorInventory.NON_INPUT_SLOTS];
        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (var slot = ProcessorInventory.NON_INPUT_SLOTS; slot < inv.getContainerSize(); slot++) {
            var stack = inv.getItem(slot);
            if (!stack.isEmpty() && matchedContainerItems[slot - ProcessorInventory.NON_INPUT_SLOTS] == null) {
                for (var input : getInputs()) {
                    if (!matchedIngredients.contains(input.ingredient()) && input.ingredient().test(stack) &&
                        stack.getCount() >= input.count()) {
                        matchedContainerItems[slot - ProcessorInventory.NON_INPUT_SLOTS] = input.ingredient();
                        matchedIngredients.add(input.ingredient());
                    }
                }
            }
        }

        return matchedIngredients.size() == getInputs().size();
    }
}
