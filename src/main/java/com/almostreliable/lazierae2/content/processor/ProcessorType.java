package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Config.ProcessorConfig;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.util.Lazy;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.Blocks.*;
import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public enum ProcessorType implements RecipeType<ProcessorRecipe> {

    AGGREGATOR(
        AGGREGATOR_ID,
        3,
        () -> Config.COMMON.aggregator,
        () -> Blocks.AGGREGATOR,
        TripleInputRecipe::new,
        () -> Serializers.AGGREGATOR
    ), CENTRIFUGE(
        CENTRIFUGE_ID,
        1,
        () -> Config.COMMON.centrifuge,
        () -> Blocks.CENTRIFUGE,
        SingleInputRecipe::new,
        () -> Serializers.CENTRIFUGE
    ), ENERGIZER(
        ENERGIZER_ID,
        1,
        () -> Config.COMMON.energizer,
        () -> Blocks.ENERGIZER,
        SingleInputRecipe::new,
        () -> Serializers.ENERGIZER
    ), ETCHER(
        ETCHER_ID,
        3,
        () -> Config.COMMON.etcher,
        () -> Blocks.ETCHER,
        TripleInputRecipe::new,
        () -> Serializers.ETCHER
    );

    private final String id;
    private final int inputSlots;
    private final Lazy<ProcessorConfig> processorConfig;
    private final Supplier<? extends Supplier<ProcessorBlock>> itemProvider;
    private final BiFunction<ResourceLocation, ProcessorType, ProcessorRecipe> recipeFactory;
    private final Supplier<? extends Supplier<RecipeSerializer<ProcessorRecipe>>> recipeSerializer;

    ProcessorType(
        String id, int inputSlots, Supplier<ProcessorConfig> processorConfig,
        Supplier<? extends Supplier<ProcessorBlock>> itemProvider,
        BiFunction<ResourceLocation, ProcessorType, ProcessorRecipe> recipeFactory,
        Supplier<? extends Supplier<RecipeSerializer<ProcessorRecipe>>> recipeSerializer
    ) {
        this.id = id;
        this.inputSlots = inputSlots;
        this.processorConfig = Lazy.of(processorConfig);
        this.itemProvider = itemProvider;
        this.recipeFactory = recipeFactory;
        this.recipeSerializer = recipeSerializer;
        Registry.register(Registry.RECIPE_TYPE, f("{}:{}", MOD_ID, id), this);
    }

    @Override
    public String toString() {
        return id;
    }

    public ItemLike getItemProvider() {
        return itemProvider.get().get();
    }

    public String getId() {
        return id;
    }

    public int getBaseProcessTime() {
        return processorConfig.get().baseProcessTime.get();
    }

    public int getBaseEnergyCost() {
        return processorConfig.get().baseEnergyUsage.get();
    }

    public int getUpgradeSlots() {
        return processorConfig.get().upgradeSlots.get();
    }

    public int getBaseEnergyBuffer() {
        return processorConfig.get().baseEnergyBuffer.get();
    }

    public int getEnergyBufferAdd() {
        return processorConfig.get().energyBufferAdd.get();
    }

    public BiFunction<ResourceLocation, ProcessorType, ProcessorRecipe> getRecipeFactory() {
        return recipeFactory;
    }

    public Supplier<RecipeSerializer<ProcessorRecipe>> getRecipeSerializer() {
        return recipeSerializer.get();
    }

    public int getInputSlots() {
        return inputSlots;
    }

    public double getProcessTimeMultiplier() {
        return processorConfig.get().processTimeMulti.get();
    }

    public double getEnergyCostMultiplier() {
        return processorConfig.get().energyUsageMulti.get();
    }
}
