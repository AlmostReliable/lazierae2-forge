package com.github.almostreliable.lazierae2.recipe;

import com.github.almostreliable.lazierae2.core.Setup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public enum MachineType implements IRecipeType<MachineRecipe> {
    AGGREGATOR("aggregator", 3, 200, 1000, TripleInputRecipe::new, () -> Setup.Recipes.Serializers.AGGREGATOR),
    CENTRIFUGE("centrifuge", 1, 200, 1000, SingleInputRecipe::new, () -> Setup.Recipes.Serializers.CENTRIFUGE),
    ENERGIZER("energizer", 1, 200, 1000, SingleInputRecipe::new, () -> Setup.Recipes.Serializers.ENERGIZER),
    ETCHER("etcher",3, 200, 1000, TripleInputRecipe::new, () -> Setup.Recipes.Serializers.ETCHER);

    private final String id;
    private final int inputSlots;
    private final int processingTime;
    private final int energyCost;
    private final BiFunction<ResourceLocation, MachineType, MachineRecipe> factory;
    private final Supplier<Supplier<IRecipeSerializer<MachineRecipe>>> serializer;

    MachineType(
        String id, int inputSlots, int processingTime, int energyCost, BiFunction<ResourceLocation, MachineType, MachineRecipe> factory,
        Supplier<Supplier<IRecipeSerializer<MachineRecipe>>> serializer
    ) {
        this.id = id;
        this.inputSlots = inputSlots;
        this.processingTime = processingTime;
        this.energyCost = energyCost;
        this.factory = factory;
        this.serializer = serializer;
    }

    public String getId() {
        return id;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    public BiFunction<ResourceLocation, MachineType, MachineRecipe> getFactory() {
        return factory;
    }

    public Supplier<IRecipeSerializer<MachineRecipe>> getSerializer() {
        return serializer.get();
    }

    @Override
    public String toString() {
        return id;
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
