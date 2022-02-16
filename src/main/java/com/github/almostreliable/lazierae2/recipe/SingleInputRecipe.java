package com.github.almostreliable.lazierae2.recipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SingleInputRecipe extends MachineRecipe {

    public SingleInputRecipe(ResourceLocation id, MachineType machineType) {
        super(id, machineType);
    }

    @Override
    public boolean matches(IInventory inv, World level) {
        return inputs.size() == 1 && inputs.get(0).test(inv.getItem(inv.getContainerSize() - 1));
    }
}
