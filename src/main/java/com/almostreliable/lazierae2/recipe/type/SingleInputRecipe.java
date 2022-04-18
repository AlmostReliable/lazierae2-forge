package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public class SingleInputRecipe extends ProcessorRecipe {

    public SingleInputRecipe(ResourceLocation id, ProcessorType processorType) {
        super(id, processorType);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return inputs.size() == 1 && inputs.get(0).test(inv.getItem(inv.getContainerSize() - 1));
    }
}
