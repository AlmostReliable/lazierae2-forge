package com.github.almostreliable.lazierae2.data.server;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.util.AEColor;
import appeng.core.Api;
import com.github.almostreliable.lazierae2.core.Setup;
import com.github.almostreliable.lazierae2.recipe.builder.MachineRecipeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.data.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeData extends RecipeProvider {

    private static final IBlocks AE_BLOCKS = Api.instance().definitions().blocks();
    private static final IMaterials AE_MATERIALS = Api.instance().definitions().materials();
    private static final IParts AE_PARTS = Api.instance().definitions().parts();
    private static final String HAS_CONDITION = "has_item";

    public RecipeData(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> c) {
        shapedBlocks(c);
        shapedItems(c);
        shapelessItems(c);
        cookingItems(c);
        aggregatorRecipes(c);
    }

    private void shapedBlocks(Consumer<IFinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.AGGREGATOR.get())
            .pattern("fmf")
            .pattern("ouo")
            .pattern("lcl")
            .define('f', AE_MATERIALS.fluixPearl().item())
            .define('m', AE_BLOCKS.molecularAssembler().item())
            .define('o', Blocks.OBSERVER)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('l', AE_MATERIALS.logicProcessor().item())
            .define('c', AE_BLOCKS.condenser().item())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.CENTRIFUGE.get())
            .pattern("fsf")
            .pattern("pup")
            .pattern("fcf")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('s', Setup.Tags.Items.PROCESSOR_SPEC)
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('c', Setup.Items.GROWTH_CHAMBER.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.ENERGIZER.get())
            .pattern("fcf")
            .pattern("quq")
            .pattern("fdf")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('c', AE_BLOCKS.charger().item())
            .define('q', AE_PARTS.quartzFiber().item())
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('d', AE_BLOCKS.energyCell().item())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.ETCHER.get())
            .pattern("fif")
            .pattern("ili")
            .pattern("pup")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('i', AE_BLOCKS.inscriber().item())
            .define('l', Setup.Items.LOGIC_UNIT.get())
            .define('p', Setup.Tags.Items.PROCESSOR_SPEC)
            .define('u', Setup.Items.UNIVERSAL_PRESS.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
    }

    private void shapedItems(Consumer<IFinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Setup.Items.LOGIC_UNIT.get())
            .pattern("sgs")
            .pattern("dpd")
            .pattern("sgs")
            .define('s', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('g', AE_BLOCKS.quartzGlass().item())
            .define('d', Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .define('p', AE_MATERIALS.engProcessor().item())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.GROWTH_CHAMBER.get())
            .pattern("ama")
            .pattern("aba")
            .pattern("afa")
            .define('a', AE_BLOCKS.quartzGrowthAccelerator().item())
            .define('m', AE_BLOCKS.molecularAssembler().item())
            .define('b', Items.WATER_BUCKET.asItem())
            .define('f', AE_PARTS.cableGlass().item(AEColor.TRANSPARENT))
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AE_BLOCKS.quartzGrowthAccelerator().item()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.UNIVERSAL_PRESS.get())
            .pattern("ipi")
            .pattern("csl")
            .pattern("iei")
            .define('i', Setup.Tags.Items.INGOTS_FLUIX_IRON)
            .define('p', AE_MATERIALS.siliconPress().item())
            .define('c', AE_MATERIALS.calcProcessorPress().item())
            .define('s', AE_MATERIALS.singularity().item())
            .define('l', AE_MATERIALS.logicProcessorPress().item())
            .define('e', AE_MATERIALS.engProcessorPress().item())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AE_MATERIALS.singularity().item()))
            .save(c);
    }

    private void shapelessItems(Consumer<IFinishedRecipe> c) {
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.CARB_FLUIX_DUST.get())
            .requires(AE_MATERIALS.fluixDust().item(), 2)
            .requires(Setup.Tags.Items.DUSTS_COAL)
            .requires(Setup.Tags.Items.DUSTS_COAL)
            .requires(Setup.Tags.Items.SILICON)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.SILICON))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_2.get())
            .requires(Tags.Items.DUSTS_REDSTONE)
            .requires(Setup.Items.SPEC_CORE_1.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_1.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_4.get())
            .requires(Setup.Tags.Items.SILICON)
            .requires(Setup.Items.SPEC_CORE_2.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_2.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_8.get())
            .requires(AE_MATERIALS.logicProcessor().item())
            .requires(Setup.Items.SPEC_CORE_4.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_4.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_16.get())
            .requires(AE_MATERIALS.calcProcessor().item())
            .requires(Setup.Items.SPEC_CORE_8.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_8.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_32.get())
            .requires(AE_MATERIALS.engProcessor().item())
            .requires(Setup.Items.SPEC_CORE_16.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_16.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_64.get())
            .requires(Setup.Items.PARALLEL_PROCESSOR.get())
            .requires(Setup.Items.SPEC_CORE_32.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_32.get()))
            .save(c);
    }

    private void cookingItems(Consumer<IFinishedRecipe> c) {
        CookingRecipeBuilder
            .smelting(Ingredient.of(Setup.Tags.Items.INGOTS_FLUIX_IRON), Setup.Items.FLUIX_STEEL.get(), 0.15F, 120)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.INGOTS_FLUIX_IRON))
            .save(c);
    }

    private void aggregatorRecipes(Consumer<? super IFinishedRecipe> c) {
        MachineRecipeBuilder
            .aggregator(Setup.Items.FLUIX_STEEL.get())
            .input(Setup.Tags.Items.DUSTS_COAL)
            .input(AE_MATERIALS.fluixDust())
            .input(Tags.Items.INGOTS_IRON)
            .processingTime(80)
            .energyCost(1_500)
            .build(c);
        MachineRecipeBuilder
            .aggregator(Setup.Items.CARB_FLUIX_DUST.get())
            .input(Setup.Tags.Items.DUSTS_COAL)
            .input(AE_MATERIALS.fluixDust())
            .input(Setup.Tags.Items.SILICON)
            .processingTime(30)
            .energyCost(300)
            .build(c);
        MachineRecipeBuilder
            .aggregator(AE_MATERIALS.fluixCrystal(), 2)
            .input(Tags.Items.GEMS_QUARTZ)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(AE_MATERIALS.certusQuartzCrystalCharged())
            .processingTime(25)
            .energyCost(250)
            .build(c);
        MachineRecipeBuilder
            .aggregator(Setup.Items.RESONATING_GEM.get())
            .input(AE_MATERIALS.skyDust())
            .input(Tags.Items.GEMS_DIAMOND)
            .input(AE_MATERIALS.enderDust())
            .processingTime(120)
            .energyCost(2_000)
            .build(c);
        MachineRecipeBuilder
            .aggregator(Setup.Items.SPEC_CORE_1.get())
            .input(AE_MATERIALS.skyDust())
            .input(AE_MATERIALS.matterBall())
            .input(Setup.Items.CARB_FLUIX_DUST.get())
            .input(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .processingTime(60)
            .energyCost(600)
            .build(c);
    }
}
