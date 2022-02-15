package com.github.almostreliable.lazierae2.recipe.type;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class SingleInputRecipe extends MachineRecipe {

    SingleInputRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(IInventory inv, World level) {
        return inputs.size() == 1 && inputs.get(0).test(inv.getItem(inv.getContainerSize() - 1));
    }
}
