package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.component.InventoryHandler.ProcessorInventory;
import com.almostreliable.lazierae2.content.processor.ProcessorType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class TripleInputRecipe extends ProcessorRecipe {

    public TripleInputRecipe(ResourceLocation id, ProcessorType processorType) {
        super(id, processorType);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        if (inputs.isEmpty()) return false;

        var matchedContainerItems = new Ingredient[inv.getContainerSize() - ProcessorInventory.NON_INPUT_SLOTS];
        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (var slot = ProcessorInventory.NON_INPUT_SLOTS; slot < inv.getContainerSize(); slot++) {
            var stack = inv.getItem(slot);
            if (!stack.isEmpty() && matchedContainerItems[slot - ProcessorInventory.NON_INPUT_SLOTS] == null) {
                for (var input : inputs) {
                    if (!matchedIngredients.contains(input) && input.test(stack)) {
                        matchedContainerItems[slot - ProcessorInventory.NON_INPUT_SLOTS] = input;
                        matchedIngredients.add(input);
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }
}
