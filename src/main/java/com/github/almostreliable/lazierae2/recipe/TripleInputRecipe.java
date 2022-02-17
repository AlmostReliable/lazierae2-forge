package com.github.almostreliable.lazierae2.recipe;

import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.core.TypeEnums.MachineType;
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

        Ingredient[] matchedContainerItems = new Ingredient[inv.getContainerSize()];
        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (int invIndex = InventoryHandler.NON_INPUT_SLOTS; invIndex < inv.getContainerSize(); invIndex++) {
            ItemStack item = inv.getItem(invIndex);
            if (!item.isEmpty() && matchedContainerItems[invIndex] == null) {
                for (Ingredient input : inputs) {
                    if (!matchedIngredients.contains(input) && input.test(item)) {
                        matchedContainerItems[invIndex] = input;
                        matchedIngredients.add(input);
                    }
                }
            }
        }

        return matchedIngredients.size() == inputs.size();
    }
}
