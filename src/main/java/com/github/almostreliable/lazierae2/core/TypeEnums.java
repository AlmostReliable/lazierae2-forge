package com.github.almostreliable.lazierae2.core;

import com.github.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.github.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.github.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Defines the type of the translation to
     * identify its key inside the lang file.
     */
    public enum TRANSLATE_TYPE {
        BLOCK, LABEL, TOOLTIP, BLOCK_SIDE, IO_SETTING, NUMBER, STATUS, MODE, ACCURACY
    }

    /**
     * Defines the possible IO sides of a block.
     */
    public enum BLOCK_SIDE {
        BOTTOM, TOP, FRONT, BACK, LEFT, RIGHT
    }

    /**
     * Enum to represent the different IO settings for the side configuration.
     */
    public enum IO_SETTING {
        OFF, INPUT, OUTPUT, IO
    }

    public enum MachineType implements IRecipeType<MachineRecipe> {

        AGGREGATOR(AGGREGATOR_ID, 3, 200, 1_000, TripleInputRecipe::new, Serializers.AGGREGATOR),
        CENTRIFUGE(CENTRIFUGE_ID, 1, 200, 1_000, SingleInputRecipe::new, Serializers.CENTRIFUGE),
        ENERGIZER(ENERGIZER_ID, 1, 200, 1_000, SingleInputRecipe::new, Serializers.ENERGIZER),
        ETCHER(ETCHER_ID, 3, 200, 1_000, TripleInputRecipe::new, Serializers.ETCHER);

        private final String id;
        private final int inputSlots;
        private final int processingTime;
        private final int energyCost;
        private final BiFunction<ResourceLocation, MachineType, MachineRecipe> recipeFactory;
        private final Supplier<IRecipeSerializer<MachineRecipe>> recipeSerializer;

        MachineType(
            String id, int inputSlots, int processingTime, int energyCost,
            BiFunction<ResourceLocation, MachineType, MachineRecipe> recipeFactory,
            Supplier<IRecipeSerializer<MachineRecipe>> recipeSerializer
        ) {
            this.id = id;
            this.inputSlots = inputSlots;
            this.processingTime = processingTime;
            this.energyCost = energyCost;
            this.recipeFactory = recipeFactory;
            this.recipeSerializer = recipeSerializer;
        }

        @Override
        public String toString() {
            return id;
        }

        public String getId() {
            return id;
        }

        public int getProcessingTime() {
            // TODO: grab from config
            return processingTime;
        }

        public int getEnergyCost() {
            // TODO: grab from config
            return energyCost;
        }

        public BiFunction<ResourceLocation, MachineType, MachineRecipe> getRecipeFactory() {
            return recipeFactory;
        }

        public Supplier<IRecipeSerializer<MachineRecipe>> getRecipeSerializer() {
            return recipeSerializer;
        }

        public int getInputSlots() {
            return inputSlots;
        }

        public double getUpgradeProcessTimeMultiplier() {
            // TODO: grab from config
            return 1.0;
        }

        public double getUpgradeEnergyMultiplier() {
            // TODO: grab from config
            return 1.0;
        }
    }
}
