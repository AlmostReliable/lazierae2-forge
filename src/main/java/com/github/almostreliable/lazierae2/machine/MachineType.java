package com.github.almostreliable.lazierae2.machine;

import com.github.almostreliable.lazierae2.core.Config;
import com.github.almostreliable.lazierae2.core.Config.MachineConfig;
import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.core.Setup.Recipes.Serializers;
import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.github.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.github.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.github.almostreliable.lazierae2.core.Constants.*;
import static com.github.almostreliable.lazierae2.util.TextUtil.f;

public enum MachineType implements IRecipeType<MachineRecipe> {

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
    private final Lazy<MachineConfig> machineConfig;
    private final Supplier<? extends Supplier<MachineBlock>> itemProvider;
    private final BiFunction<ResourceLocation, MachineType, MachineRecipe> recipeFactory;
    private final Supplier<? extends Supplier<IRecipeSerializer<MachineRecipe>>> recipeSerializer;

    MachineType(
        String id, int inputSlots, Supplier<MachineConfig> machineConfig,
        Supplier<? extends Supplier<MachineBlock>> itemProvider,
        BiFunction<ResourceLocation, MachineType, MachineRecipe> recipeFactory,
        Supplier<? extends Supplier<IRecipeSerializer<MachineRecipe>>> recipeSerializer
    ) {
        this.id = id;
        this.inputSlots = inputSlots;
        this.machineConfig = Lazy.of(machineConfig);
        this.itemProvider = itemProvider;
        this.recipeFactory = recipeFactory;
        this.recipeSerializer = recipeSerializer;
        IRecipeType.register(f("{}:{}", MOD_ID, id));
    }

    @Override
    public String toString() {
        return id;
    }

    public IItemProvider getItemProvider() {
        return itemProvider.get().get();
    }

    public String getId() {
        return id;
    }

    public int getBaseProcessTime() {
        return machineConfig.get().baseProcessTime.get();
    }

    public int getBaseEnergyCost() {
        return machineConfig.get().baseEnergyUsage.get();
    }

    public int getUpgradeSlots() {
        return machineConfig.get().upgradeSlots.get();
    }

    public int getBaseEnergyBuffer() {
        return machineConfig.get().baseEnergyBuffer.get();
    }

    public int getEnergyBufferAdd() {
        return machineConfig.get().energyBufferAdd.get();
    }

    public BiFunction<ResourceLocation, MachineType, MachineRecipe> getRecipeFactory() {
        return recipeFactory;
    }

    public Supplier<IRecipeSerializer<MachineRecipe>> getRecipeSerializer() {
        return recipeSerializer.get();
    }

    public int getInputSlots() {
        return inputSlots;
    }

    public double getProcessTimeMultiplier() {
        return machineConfig.get().processTimeMulti.get();
    }

    public double getEnergyCostMultiplier() {
        return machineConfig.get().energyUsageMulti.get();
    }
}
