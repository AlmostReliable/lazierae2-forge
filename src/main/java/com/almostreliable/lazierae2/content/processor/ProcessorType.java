package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.core.Config;
import com.almostreliable.lazierae2.core.Config.ProcessorConfig;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Serializers;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipeFactory;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.Blocks.*;

public enum ProcessorType implements RecipeType<ProcessorRecipe> {

    AGGREGATOR(AGGREGATOR_ID, 3, () -> Config.COMMON.aggregator, () -> Blocks.AGGREGATOR, () -> Serializers.AGGREGATOR),
    ETCHER(ETCHER_ID, 3, () -> Config.COMMON.etcher, () -> Blocks.ETCHER, () -> Serializers.ETCHER),
    GRINDER(GRINDER_ID, 1, () -> Config.COMMON.grinder, () -> Blocks.GRINDER, () -> Serializers.GRINDER),
    INFUSER(INFUSER_ID, 3, () -> Config.COMMON.infuser, () -> Blocks.INFUSER, () -> Serializers.INFUSER);

    private final String id;
    private final int inputSlots;
    private final Lazy<ProcessorConfig> config;
    private final Supplier<RegistryObject<? extends ProcessorBlock>> itemProvider;
    private final Supplier<RegistryObject<? extends RecipeSerializer<ProcessorRecipe>>> recipeSerializer;

    ProcessorType(
        String id, int inputSlots, Supplier<ProcessorConfig> config,
        Supplier<RegistryObject<? extends ProcessorBlock>> itemProvider,
        Supplier<RegistryObject<? extends RecipeSerializer<ProcessorRecipe>>> recipeSerializer
    ) {
        this.id = id;
        this.inputSlots = inputSlots;
        this.config = Lazy.of(config);
        this.itemProvider = itemProvider;
        this.recipeSerializer = recipeSerializer;
        Registry.register(Registry.RECIPE_TYPE, TextUtil.getRL(id), this);
    }

    @Override
    public String toString() {
        return id;
    }

    public double getProcessTimeMultiplier(int upgrades) {
        return config.get().processTimeMulti.get().get(upgrades - 1);
    }

    public double getEnergyCostMultiplier(int upgrades) {
        return config.get().energyUsageMulti.get().get(upgrades - 1);
    }

    public ItemLike getItemProvider() {
        return itemProvider.get().get();
    }

    public String getId() {
        return id;
    }

    public int getBaseProcessTime() {
        return config.get().baseProcessTime.get();
    }

    public int getBaseEnergyCost() {
        return config.get().baseEnergyUsage.get();
    }

    public int getUpgradeSlots() {
        return config.get().upgradeSlots.get();
    }

    public int getBaseEnergyBuffer() {
        return config.get().baseEnergyBuffer.get();
    }

    public int getEnergyBufferAdd() {
        return config.get().energyBufferAdd.get();
    }

    public ProcessorRecipeFactory getRecipeFactory() {
        return inputSlots == 1 ? SingleInputRecipe::new : TripleInputRecipe::new;
    }

    public RecipeSerializer<ProcessorRecipe> getRecipeSerializer() {
        return recipeSerializer.get().get();
    }

    public int getInputSlots() {
        return inputSlots;
    }
}
