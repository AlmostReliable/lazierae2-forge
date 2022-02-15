package com.github.almostreliable.lazierae2.recipe.type;

import com.github.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.github.almostreliable.lazierae2.core.Setup.Recipes.Types;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

public class AggregatorRecipe extends TripleInputRecipe {

    public AggregatorRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Serializers.AGGREGATOR.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return Types.AGGREGATOR;
    }
}
