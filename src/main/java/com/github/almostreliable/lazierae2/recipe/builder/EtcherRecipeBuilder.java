package com.github.almostreliable.lazierae2.recipe.builder;

import com.github.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

import static com.github.almostreliable.lazierae2.core.Constants.ETCHER_ID;

public class EtcherRecipeBuilder extends MachineRecipeBuilder<EtcherRecipeBuilder> {

    EtcherRecipeBuilder(IItemProvider output, int outputCount) {
        super(output, outputCount);
    }

    @Override
    protected void validateProcessingTime() {
        // TODO: read from config
        if (processingTime == 0) processingTime = 200;
    }

    @Override
    protected void validateEnergyCost() {
        // TODO: read from config
        if (energyCost == 0) energyCost = 1_000;
    }

    @Override
    protected void build(Consumer<? super IFinishedRecipe> consumer, ResourceLocation recipeId) {
        consumer.accept(new Recipe(this, recipeId));
    }

    @Override
    protected String getMachineId() {
        return ETCHER_ID;
    }

    private static final class Recipe extends FinishedMachineRecipe<EtcherRecipeBuilder> {

        private Recipe(EtcherRecipeBuilder builder, ResourceLocation recipeId) {
            super(builder, recipeId, builder.getMachineId());
        }

        @Override
        public IRecipeSerializer<?> getType() {
            return Serializers.ETCHER.get();
        }
    }
}
