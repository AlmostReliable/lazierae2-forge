package com.github.almostreliable.lazierae2.recipe;

import com.github.almostreliable.lazierae2.component.InventoryHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;

public class TripleInputRecipe extends MachineRecipe {

    public TripleInputRecipe(ResourceLocation id, MachineType machineType) {
        super(id, machineType);
    }

    @SuppressWarnings("Convert2streamapi")
    @Override
    public boolean matches(IInventory inv, World level) {
        if (inputs.isEmpty()) return false;

        Collection<ItemStack> containerInputs = new ArrayList<>();
        for (int i = InventoryHandler.NON_INPUT_SLOTS; i < inv.getContainerSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item.isEmpty()) continue;
            containerInputs.add(item);
        }

        if (containerInputs.size() != inputs.size()) return false;

        for (Ingredient input : inputs) {
            for (ItemStack containerItem : containerInputs) {
                // noinspection ConfusingElseBranch
                if (input.test(containerItem)) {
                    containerInputs.remove(containerItem);
                    break;
                } else {
                    return false;
                }
            }
        }

        return true;
    }
}
