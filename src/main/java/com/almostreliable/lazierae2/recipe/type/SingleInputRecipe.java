package com.almostreliable.lazierae2.recipe.type;

import com.almostreliable.lazierae2.content.processor.ProcessorType;
import com.almostreliable.lazierae2.recipe.IRecipeItemProvider;
import com.almostreliable.lazierae2.recipe.IngredientWithCount;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.List;

public class SingleInputRecipe extends ProcessorRecipe {

    public SingleInputRecipe(
        ResourceLocation recipeId, ProcessorType processorType, List<ICondition> conditions, IRecipeItemProvider output,
        NonNullList<IngredientWithCount> inputs, int processTime, int energyCost
    ) {
        super(recipeId, processorType, conditions, output, inputs, processTime, energyCost);
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return getInputs().size() == 1 &&
            getInputs().get(0).ingredient().test(inv.getItem(inv.getContainerSize() - 1)) &&
            inv.getItem(inv.getContainerSize() - 1).getCount() >= getInputs().get(0).count();
    }
}
