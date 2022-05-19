package com.almostreliable.lazierae2.data.server;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeData extends RecipeProvider {

    private static final String HAS_CONDITION = "has_item";

    public RecipeData(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> c) {
        shapedBlocks(c);
        shapedItems(c);
        shapelessItems(c);
        cookingItems(c);
        aggregatorRecipes(c);
        centrifugeRecipes(c);
        energizerRecipes(c);
        etcherRecipes(c);
    }

    private void shapedBlocks(Consumer<FinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.AGGREGATOR.get())
            .pattern("fmf")
            .pattern("ouo")
            .pattern("lcl")
            .define('f', AEItems.FLUIX_PEARL.asItem())
            .define('m', AEBlocks.MOLECULAR_ASSEMBLER.asItem())
            .define('o', Blocks.OBSERVER)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('l', AEItems.LOGIC_PROCESSOR.asItem())
            .define('c', AEBlocks.CONDENSER.asItem())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.GRINDER.get())
            .pattern("fsf")
            .pattern("pup")
            .pattern("fcf")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('s', Setup.Tags.Items.PROCESSOR_SPEC)
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('c', Setup.Items.GROWTH_CORE.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.INFUSER.get())
            .pattern("fcf")
            .pattern("quq")
            .pattern("fdf")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('c', AEBlocks.CHARGER.asItem())
            .define('q', AEParts.QUARTZ_FIBER.asItem())
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('d', AEBlocks.ENERGY_CELL.asItem())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Blocks.ETCHER.get())
            .pattern("fif")
            .pattern("ili")
            .pattern("pup")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('i', AEBlocks.INSCRIBER.asItem())
            .define('l', Setup.Items.LOGIC_UNIT.get())
            .define('p', Setup.Tags.Items.PROCESSOR_SPEC)
            .define('u', Setup.Items.UNIVERSAL_PRESS.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
    }

    private void shapedItems(Consumer<FinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Setup.Items.LOGIC_UNIT.get())
            .pattern("sgs")
            .pattern("dpd")
            .pattern("sgs")
            .define('s', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('g', AEBlocks.QUARTZ_GLASS.asItem())
            .define('d', Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .define('p', AEItems.ENGINEERING_PROCESSOR.asItem())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.GROWTH_CORE.get())
            .pattern("ama")
            .pattern("aba")
            .pattern("afa")
            .define('a', AEBlocks.QUARTZ_GROWTH_ACCELERATOR.asItem())
            .define('m', AEBlocks.MOLECULAR_ASSEMBLER.asItem())
            .define('b', Items.WATER_BUCKET.asItem())
            .define('f', AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT))
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AEBlocks.QUARTZ_GROWTH_ACCELERATOR.asItem()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.UNIVERSAL_PRESS.get())
            .pattern("ipi")
            .pattern("csl")
            .pattern("iei")
            .define('i', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('p', AEItems.SILICON_PRESS.asItem())
            .define('c', AEItems.CALCULATION_PROCESSOR_PRESS.asItem())
            .define('s', AEItems.SINGULARITY.asItem())
            .define('l', AEItems.LOGIC_PROCESSOR_PRESS.asItem())
            .define('e', AEItems.ENGINEERING_PROCESSOR_PRESS.asItem())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AEItems.SINGULARITY.asItem()))
            .save(c);
    }

    private void shapelessItems(Consumer<FinishedRecipe> c) {
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.CARB_FLUIX_DUST.get())
            .requires(AEItems.FLUIX_DUST.asItem(), 2)
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
            .requires(AEItems.LOGIC_PROCESSOR.asItem())
            .requires(Setup.Items.SPEC_CORE_4.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_4.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_16.get())
            .requires(AEItems.CALCULATION_PROCESSOR.asItem())
            .requires(Setup.Items.SPEC_CORE_8.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_8.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_32.get())
            .requires(AEItems.ENGINEERING_PROCESSOR.asItem())
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

    private void cookingItems(Consumer<FinishedRecipe> c) {
        SimpleCookingRecipeBuilder
            .smelting(Ingredient.of(Setup.Tags.Items.INGOTS_FLUIX_STEEL), Setup.Items.FLUIX_STEEL.get(), 0.15F, 120)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.INGOTS_FLUIX_STEEL))
            .save(c);
    }

    private void aggregatorRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.FLUIX_STEEL.get())
            .input(Setup.Tags.Items.DUSTS_COAL)
            .input(AEItems.FLUIX_DUST.asItem())
            .input(Tags.Items.INGOTS_IRON)
            .processingTime(80)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.CARB_FLUIX_DUST.get())
            .input(Setup.Tags.Items.DUSTS_COAL)
            .input(AEItems.FLUIX_DUST.asItem())
            .input(Setup.Tags.Items.SILICON)
            .processingTime(30)
            .energyCost(300)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(AEItems.FLUIX_CRYSTAL.asItem(), 2)
            .input(Tags.Items.GEMS_QUARTZ)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
            .processingTime(25)
            .energyCost(250)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.RESONATING_CRYSTAL.get())
            .input(AEItems.SKY_DUST.asItem())
            .input(Tags.Items.GEMS_DIAMOND)
            .input(AEItems.ENDER_DUST.asItem())
            .processingTime(120)
            .energyCost(2_000)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.SPEC_CORE_1.get())
            .input(AEItems.SKY_DUST.asItem())
            .input(AEItems.MATTER_BALL.asItem())
            .input(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .processingTime(60)
            .energyCost(600)
            .build(c);
    }

    private void centrifugeRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .grinder(AEItems.SKY_DUST.asItem())
            .input(AEBlocks.SKY_STONE_BLOCK.asItem())
            .processingTime(80)
            .energyCost(800)
            .build(c);
        ProcessorRecipeBuilder
            .grinder(AEItems.ENDER_DUST.asItem())
            .input(Tags.Items.ENDER_PEARLS)
            .processingTime(80)
            .energyCost(800)
            .build(c);
    }

    private void energizerRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .infuser(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem())
            .processingTime(80)
            .energyCost(1_500)
            .build(c);
    }

    private void etcherRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .etcher(AEItems.LOGIC_PROCESSOR.asItem())
            .input(Tags.Items.INGOTS_GOLD)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Setup.Tags.Items.SILICON)
            .processingTime(100)
            .energyCost(1_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.CALCULATION_PROCESSOR.asItem())
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem())
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Setup.Tags.Items.SILICON)
            .processingTime(100)
            .energyCost(1_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.ENGINEERING_PROCESSOR.asItem())
            .input(Tags.Items.GEMS_DIAMOND)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Setup.Tags.Items.SILICON)
            .processingTime(100)
            .energyCost(1_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.PARALLEL_PROCESSOR.get())
            .input(Setup.Tags.Items.GEMS_RESONATING)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Setup.Tags.Items.SILICON)
            .processingTime(150)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.SPEC_PROCESSOR.get())
            .input(Setup.Items.SPEC_CORE_64.get())
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Setup.Tags.Items.SILICON)
            .processingTime(150)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.FLUIX_STEEL.get())
            .input(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .input(Tags.Items.INGOTS_IRON)
            .input(AEItems.SKY_DUST.asItem())
            .processingTime(40)
            .energyCost(200)
            .build(c);
    }
}
