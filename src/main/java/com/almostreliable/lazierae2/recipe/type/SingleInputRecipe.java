package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.machine.MachineType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public class SingleInputRecipe extends MachineRecipe {

    public SingleInputRecipe(ResourceLocation id, MachineType machineType) {
        super(id, machineType);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return inputs.size() == 1 && inputs.get(0).test(inv.getItem(inv.getContainerSize() - 1));
    }
}
