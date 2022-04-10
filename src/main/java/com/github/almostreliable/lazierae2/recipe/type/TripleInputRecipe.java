package com.github.almostreliable.lazierae2.recipe.type;

import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.machine.MachineType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class TripleInputRecipe extends MachineRecipe {

    public TripleInputRecipe(ResourceLocation id, MachineType machineType) {
        super(id, machineType);
    }

    @Override
    public boolean matches(IInventory inv, World level) {
        if (inputs.isEmpty()) return false;

        Ingredient[] matchedContainerItems = new Ingredient[inv.getContainerSize() - InventoryHandler.NON_INPUT_SLOTS];
        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (int slot = InventoryHandler.NON_INPUT_SLOTS; slot < inv.getContainerSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty() && matchedContainerItems[slot - InventoryHandler.NON_INPUT_SLOTS] == null) {
                for (Ingredient input : inputs) {
                    if (!matchedIngredients.contains(input) && input.test(stack)) {
                        matchedContainerItems[slot - InventoryHandler.NON_INPUT_SLOTS] = input;
                        matchedIngredients.add(input);
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }
}
