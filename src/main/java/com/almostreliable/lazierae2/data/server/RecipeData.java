package com.almostreliable.lazierae2.data.server;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.recipe.builder.ProcessorRecipeBuilder;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static com.almostreliable.lazierae2.util.TextUtil.f;

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
        aggregatorRecipes(c);
        etcherRecipes(c);
        grinderRecipes(c);
        infuserRecipes(c);
        compatRecipes(c);
    }

    private void shapedBlocks(Consumer<FinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Blocks.AGGREGATOR.get())
            .pattern("fcf")
            .pattern("pup")
            .pattern("ege")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('c', AEBlocks.CHARGER)
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('e', AEBlocks.ENERGY_CELL)
            .define('g', Setup.Items.GROWTH_CORE.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Blocks.ETCHER.get())
            .pattern("fif")
            .pattern("ili")
            .pattern("pup")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('i', AEBlocks.INSCRIBER)
            .define('l', Setup.Items.LOGIC_UNIT.get())
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('u', Setup.Items.UNIVERSAL_PRESS.get())
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Blocks.GRINDER.get())
            .pattern("vbv")
            .pattern("aua")
            .pattern("fif")
            .define('v', AEBlocks.QUARTZ_VIBRANT_GLASS)
            .define('b', Items.IRON_BARS)
            .define('a', Items.AMETHYST_SHARD)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('i', Tags.Items.STORAGE_BLOCKS_IRON)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Blocks.INFUSER.get())
            .pattern("faf")
            .pattern("opo")
            .pattern("ece")
            .define('f', AEItems.FLUIX_PEARL)
            .define('a', AEBlocks.MOLECULAR_ASSEMBLER)
            .define('o', Items.OBSERVER)
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('e', AEItems.ENGINEERING_PROCESSOR)
            .define('c', AEBlocks.CONDENSER)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.PROCESSOR_PARALLEL))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Blocks.REQUESTER.get())
            .pattern("cic")
            .pattern("pup")
            .pattern("fef")
            .define('c', ConventionTags.MEMORY_CARDS)
            .define('i', AEBlocks.INTERFACE)
            .define('p', Setup.Tags.Items.PROCESSOR_PARALLEL)
            .define('u', Setup.Items.LOGIC_UNIT.get())
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('e', AEBlocks.DENSE_ENERGY_CELL)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.LOGIC_UNIT.get()))
            .save(c);
    }

    private void shapedItems(Consumer<FinishedRecipe> c) {
        ShapedRecipeBuilder
            .shaped(Setup.Items.LOGIC_UNIT.get())
            .pattern("sps")
            .pattern("fff")
            .pattern("qqq")
            .define('s', ConventionTags.SILICON)
            .define('p', AEItems.LOGIC_PROCESSOR)
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('q', AEParts.QUARTZ_FIBER)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.INGOTS_FLUIX_STEEL))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.GROWTH_CORE.get())
            .pattern("ama")
            .pattern("aba")
            .pattern("aga")
            .define('a', AEBlocks.QUARTZ_GROWTH_ACCELERATOR)
            .define('m', AEBlocks.MOLECULAR_ASSEMBLER)
            .define('b', Items.WATER_BUCKET)
            .define('g', ConventionTags.GLASS_CABLE)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AEBlocks.QUARTZ_GROWTH_ACCELERATOR))
            .save(c);
        ShapedRecipeBuilder
            .shaped(Setup.Items.UNIVERSAL_PRESS.get())
            .pattern("fpf")
            .pattern("csl")
            .pattern("fef")
            .define('f', Setup.Tags.Items.INGOTS_FLUIX_STEEL)
            .define('p', AEItems.SILICON_PRESS)
            .define('c', AEItems.CALCULATION_PROCESSOR_PRESS)
            .define('s', AEItems.SINGULARITY)
            .define('l', AEItems.LOGIC_PROCESSOR_PRESS)
            .define('e', AEItems.ENGINEERING_PROCESSOR_PRESS)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(AEItems.SINGULARITY))
            .save(c);
    }

    private void shapelessItems(Consumer<FinishedRecipe> c) {
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.RESONATING_SEED.get())
            .requires(Tags.Items.SAND)
            .requires(Setup.Tags.Items.DUSTS_RESONATING)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Tags.Items.DUSTS_RESONATING))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.CARB_FLUIX_DUST.get())
            .requires(AEItems.FLUIX_DUST, 2)
            .requires(Setup.Tags.Items.DUSTS_COAL)
            .requires(Setup.Tags.Items.DUSTS_COAL)
            .requires(ConventionTags.SILICON)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(ConventionTags.SILICON))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_2.get())
            .requires(Tags.Items.DUSTS_REDSTONE)
            .requires(Setup.Items.SPEC_CORE_1.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_1.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_4.get())
            .requires(ConventionTags.SILICON)
            .requires(Setup.Items.SPEC_CORE_2.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_2.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_8.get())
            .requires(AEItems.LOGIC_PROCESSOR)
            .requires(Setup.Items.SPEC_CORE_4.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_4.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_16.get())
            .requires(AEItems.CALCULATION_PROCESSOR)
            .requires(Setup.Items.SPEC_CORE_8.get(), 2)
            .unlockedBy(HAS_CONDITION, RecipeProvider.has(Setup.Items.SPEC_CORE_8.get()))
            .save(c);
        ShapelessRecipeBuilder
            .shapeless(Setup.Items.SPEC_CORE_32.get())
            .requires(AEItems.ENGINEERING_PROCESSOR)
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

    private void aggregatorRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .aggregator(AEItems.CERTUS_QUARTZ_CRYSTAL, 2)
            .input(Tags.Items.SAND)
            .input(ConventionTags.CERTUS_QUARTZ_DUST)
            .processingTime(200)
            .energyCost(12_000)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(AEItems.FLUIX_CRYSTAL, 2)
            .input(Tags.Items.SAND)
            .input(ConventionTags.FLUIX_DUST)
            .processingTime(200)
            .energyCost(12_000)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.RESONATING_CRYSTAL.get(), 2)
            .input(Tags.Items.SAND)
            .input(Setup.Tags.Items.DUSTS_RESONATING)
            .processingTime(200)
            .energyCost(12_000)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(AEItems.FLUIX_DUST, 2)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
            .input(Tags.Items.GEMS_QUARTZ)
            .processingTime(4)
            .energyCost(50)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(Setup.Items.RESONATING_DUST.get(), 2)
            .input(AEItems.SKY_DUST)
            .input(Tags.Items.GEMS_DIAMOND)
            .input(ConventionTags.ENDER_PEARL_DUST)
            .processingTime(4)
            .energyCost(80)
            .build(c);
        ProcessorRecipeBuilder
            .aggregator(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL)
            .processingTime(25)
            .energyCost(1_500)
            .build(c);
    }

    private void etcherRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .etcher(AEItems.LOGIC_PROCESSOR)
            .input(Tags.Items.INGOTS_GOLD)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(ConventionTags.SILICON)
            .processingTime(80)
            .energyCost(750)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.CALCULATION_PROCESSOR)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(ConventionTags.SILICON)
            .processingTime(80)
            .energyCost(750)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.ENGINEERING_PROCESSOR)
            .input(Tags.Items.GEMS_DIAMOND)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(ConventionTags.SILICON)
            .processingTime(80)
            .energyCost(750)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.PARALLEL_PROCESSOR.get())
            .input(Setup.Tags.Items.GEMS_RESONATING)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(ConventionTags.SILICON)
            .processingTime(120)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.SPEC_PROCESSOR.get())
            .input(Setup.Items.SPEC_CORE_64.get())
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(ConventionTags.SILICON)
            .processingTime(150)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.LOGIC_PROCESSOR_PRESS, 2)
            .input(AEItems.LOGIC_PROCESSOR_PRESS)
            .input(Tags.Items.INGOTS_IRON, 6)
            .processingTime(200)
            .energyCost(5_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.CALCULATION_PROCESSOR_PRESS, 2)
            .input(AEItems.CALCULATION_PROCESSOR_PRESS)
            .input(Tags.Items.INGOTS_IRON, 6)
            .processingTime(200)
            .energyCost(5_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.ENGINEERING_PROCESSOR_PRESS, 2)
            .input(AEItems.ENGINEERING_PROCESSOR_PRESS)
            .input(Tags.Items.INGOTS_IRON, 6)
            .processingTime(200)
            .energyCost(5_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(AEItems.SILICON_PRESS, 2)
            .input(AEItems.SILICON_PRESS)
            .input(Tags.Items.INGOTS_IRON, 6)
            .processingTime(200)
            .energyCost(5_000)
            .build(c);
        ProcessorRecipeBuilder
            .etcher(Setup.Items.UNIVERSAL_PRESS.get(), 2)
            .input(Setup.Items.UNIVERSAL_PRESS.get())
            .input(Tags.Items.INGOTS_IRON, 6)
            .processingTime(300)
            .energyCost(8_000)
            .build(c);
    }

    private void grinderRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .grinder(AEItems.CERTUS_QUARTZ_DUST, 6)
            .input(ConventionTags.CERTUS_QUARTZ_ORE)
            .processingTime(50)
            .energyCost(850)
            .build(c, f("/{}", GameUtil.getIdFromItem(AEBlocks.QUARTZ_ORE.asItem())));
        ProcessorRecipeBuilder
            .grinder(AEItems.SKY_DUST)
            .input(AEBlocks.SKY_STONE_BLOCK)
            .processingTime(25)
            .energyCost(300)
            .build(c);
        ProcessorRecipeBuilder
            .grinder(AEItems.ENDER_DUST)
            .input(Tags.Items.ENDER_PEARLS)
            .processingTime(25)
            .energyCost(350)
            .build(c);
        ProcessorRecipeBuilder
            .grinder(Setup.Items.COAL_DUST.get())
            .input(ItemTags.COALS)
            .processingTime(20)
            .energyCost(200)
            .build(c);
        ProcessorRecipeBuilder
            .grinder(AEItems.FLUIX_DUST)
            .input(ConventionTags.FLUIX_CRYSTAL)
            .processingTime(30)
            .energyCost(500)
            .build(c);
        ProcessorRecipeBuilder
            .grinder(AEItems.CERTUS_QUARTZ_DUST)
            .input(ConventionTags.CERTUS_QUARTZ)
            .processingTime(30)
            .energyCost(500)
            .build(c, f("/{}", GameUtil.getIdFromItem(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem())));
        ProcessorRecipeBuilder
            .grinder(Setup.Items.RESONATING_DUST.get())
            .input(Setup.Tags.Items.GEMS_RESONATING)
            .processingTime(35)
            .energyCost(650)
            .build(c);
    }

    private void infuserRecipes(Consumer<? super FinishedRecipe> c) {
        ProcessorRecipeBuilder
            .infuser(Setup.Items.CARB_FLUIX_DUST.get())
            .input(Setup.Tags.Items.DUSTS_COAL)
            .input(ConventionTags.FLUIX_DUST)
            .input(ConventionTags.SILICON)
            .processingTime(30)
            .energyCost(300)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(Setup.Items.FLUIX_STEEL.get())
            .input(ConventionTags.FLUIX_CRYSTAL)
            .input(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX, 2)
            .input(Tags.Items.INGOTS_IRON)
            .processingTime(80)
            .energyCost(1_500)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(Setup.Items.SPEC_CORE_1.get())
            .input(AEItems.SKY_DUST)
            .input(AEItems.MATTER_BALL)
            .input(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX)
            .processingTime(60)
            .energyCost(600)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(AEItems.FLUIX_PEARL)
            .input(AEBlocks.FLUIX_BLOCK)
            .input(Tags.Items.ENDER_PEARLS)
            .input(ConventionTags.FLUIX_DUST, 2)
            .processingTime(60)
            .energyCost(800)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(AEBlocks.SKY_STONE_BLOCK)
            .input(Items.BLACK_CONCRETE_POWDER)
            .input(AEItems.SKY_DUST)
            .processingTime(160)
            .energyCost(1_800)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(AEBlocks.QUARTZ_GLASS)
            .input(ConventionTags.CERTUS_QUARTZ_DUST)
            .input(Tags.Items.GLASS)
            .processingTime(50)
            .energyCost(600)
            .build(c);
        ProcessorRecipeBuilder
            .infuser(AEBlocks.QUARTZ_VIBRANT_GLASS)
            .input(ConventionTags.CERTUS_QUARTZ_DUST)
            .input(Tags.Items.GLASS)
            .input(Tags.Items.DUSTS_GLOWSTONE)
            .processingTime(50)
            .energyCost(1_000)
            .build(c);
    }

    private void compatRecipes(Consumer<? super FinishedRecipe> c) {
        compatGrinderRecipes(c);
        compatInfuserRecipes(c);
    }

    private void compatGrinderRecipes(Consumer<? super FinishedRecipe> c) {
        /*
            MEKANISM
         */
        ProcessorRecipeBuilder
            .grinder("mekanism:sawdust")
            .input(ItemTags.LOGS)
            .processingTime(15)
            .energyCost(180)
            .build(c);
        ProcessorRecipeBuilder
            .grinder("mekanism:dirty_netherite_scrap", 3)
            .input(Tags.Items.ORES_NETHERITE_SCRAP)
            .processingTime(50)
            .energyCost(500)
            .build(c);
        ProcessorRecipeBuilder
            .grinder("mekanism:dust_netherite")
            .input(Tags.Items.INGOTS_NETHERITE)
            .processingTime(60)
            .energyCost(500)
            .build(c);
        ProcessorRecipeBuilder
            .grinder("mekanism:dust_obsidian", 4)
            .input(Tags.Items.OBSIDIAN)
            .processingTime(40)
            .energyCost(400)
            .build(c);
        mekanismGrinderBiofuel(c, 8, Items.CAKE, Items.PUMPKIN_PIE);
        mekanismGrinderBiofuel(
            c,
            7,
            Items.BAKED_POTATO,
            Items.BREAD,
            Items.BROWN_MUSHROOM_BLOCK,
            Items.COOKIE,
            Items.FLOWERING_AZALEA,
            Items.HAY_BLOCK,
            Items.NETHER_WART_BLOCK,
            Items.RED_MUSHROOM_BLOCK,
            Items.WARPED_WART_BLOCK
        );
        mekanismGrinderBiofuel(
            c,
            5,
            Items.ALLIUM,
            Items.APPLE,
            Items.AZALEA,
            Items.AZURE_BLUET,
            Items.BEETROOT,
            Items.BIG_DRIPLEAF,
            Items.BLUE_ORCHID,
            Items.BROWN_MUSHROOM,
            Items.CARROT,
            Items.CARVED_PUMPKIN,
            Items.COCOA_BEANS,
            Items.CORNFLOWER,
            Items.CRIMSON_FUNGUS,
            Items.CRIMSON_ROOTS,
            Items.DANDELION,
            Items.FERN,
            Items.LARGE_FERN,
            Items.LILAC,
            Items.LILY_OF_THE_VALLEY,
            Items.LILY_PAD,
            Items.MELON,
            Items.MOSS_BLOCK,
            Items.MUSHROOM_STEM,
            Items.NETHER_WART,
            Items.ORANGE_TULIP,
            Items.OXEYE_DAISY,
            Items.PEONY,
            Items.PINK_TULIP,
            Items.POPPY,
            Items.POTATO,
            Items.PUMPKIN,
            Items.RED_MUSHROOM,
            Items.RED_TULIP,
            Items.ROSE_BUSH,
            Items.SEA_PICKLE,
            Items.SHROOMLIGHT,
            Items.SPORE_BLOSSOM,
            Items.SUNFLOWER,
            Items.WARPED_FUNGUS,
            Items.WARPED_ROOTS,
            Items.WHEAT,
            Items.WHITE_TULIP,
            Items.WITHER_ROSE
        );
        mekanismGrinderBiofuel(
            c,
            4,
            Items.CACTUS,
            Items.DRIED_KELP_BLOCK,
            Items.FLOWERING_AZALEA_LEAVES,
            Items.GLOW_LICHEN,
            Items.MELON_SLICE,
            Items.NETHER_SPROUTS,
            Items.SUGAR_CANE,
            Items.TALL_GRASS,
            Items.TWISTING_VINES,
            Items.VINE,
            Items.WEEPING_VINES
        );
        mekanismGrinderBiofuel(
            c,
            2,
            Items.ACACIA_LEAVES,
            Items.ACACIA_SAPLING,
            Items.AZALEA_LEAVES,
            Items.BEETROOT_SEEDS,
            Items.BIRCH_LEAVES,
            Items.BIRCH_SAPLING,
            Items.DARK_OAK_LEAVES,
            Items.DARK_OAK_SAPLING,
            Items.DRIED_KELP,
            Items.GLOW_BERRIES,
            Items.GRASS,
            Items.HANGING_ROOTS,
            Items.JUNGLE_LEAVES,
            Items.JUNGLE_SAPLING,
            Items.KELP,
            Items.MELON_SEEDS,
            Items.MOSS_CARPET,
            Items.OAK_LEAVES,
            Items.OAK_SAPLING,
            Items.PUMPKIN_SEEDS,
            Items.SEAGRASS,
            Items.SMALL_DRIPLEAF,
            Items.SPRUCE_LEAVES,
            Items.SPRUCE_SAPLING,
            Items.SWEET_BERRIES,
            Items.WHEAT_SEEDS
        );
    }

    private void compatInfuserRecipes(Consumer<? super FinishedRecipe> c) {
        /*
            BOTANIA
         */
        ProcessorRecipeBuilder
            .infuser("botania:quartz_dark", 8)
            .input(Items.COAL)
            .input(Tags.Items.GEMS_QUARTZ, 6)
            .processingTime(10)
            .energyCost(100)
            .build(c);
        ProcessorRecipeBuilder
            .infuser("botania:quartz_blaze", 8)
            .input(Items.BLAZE_POWDER)
            .input(Tags.Items.GEMS_QUARTZ, 6)
            .processingTime(10)
            .energyCost(100)
            .build(c);
        ProcessorRecipeBuilder
            .infuser("botania:quartz_lavender", 8)
            .input(Items.PINK_TULIP)
            .input(Tags.Items.GEMS_QUARTZ, 6)
            .processingTime(10)
            .energyCost(100)
            .build(c);
        ProcessorRecipeBuilder
            .infuser("botania:quartz_red", 8)
            .input(Tags.Items.DUSTS_REDSTONE)
            .input(Tags.Items.GEMS_QUARTZ, 6)
            .processingTime(10)
            .energyCost(100)
            .build(c);
        ProcessorRecipeBuilder
            .infuser("botania:quartz_sunny", 8)
            .input(Items.SUNFLOWER)
            .input(Tags.Items.GEMS_QUARTZ, 6)
            .processingTime(10)
            .energyCost(100)
            .build(c);

        /*
            CREATE
         */
        ProcessorRecipeBuilder
            .infuser("create:andesite_alloy")
            .input(Items.ANDESITE)
            .input(Tags.Items.NUGGETS_IRON, 2)
            .processingTime(8)
            .energyCost(50)
            .build(c);

        /*
            MEKANISM
         */
        ProcessorRecipeBuilder
            .infuser("mekanism:dye_base", 3)
            .input(Items.PAPER)
            .input(Items.CLAY_BALL)
            .processingTime(5)
            .energyCost(20)
            .build(c);
        ProcessorRecipeBuilder
            .infuser("mekanism:structural_glass")
            .input(Setup.Tags.Items.INGOTS_STEEL)
            .input(Tags.Items.SAND)
            .processingTime(15)
            .energyCost(250)
            .build(c);

        /*
            REFINED STORAGE
         */
        ProcessorRecipeBuilder
            .infuser("refinedstorage:quartz_enriched_iron", 4)
            .input(Tags.Items.INGOTS_IRON, 2)
            .input(Tags.Items.GEMS_QUARTZ)
            .processingTime(20)
            .energyCost(250)
            .build(c);

        /*
            WAYSTONES
         */
        ProcessorRecipeBuilder
            .infuser("waystones:warp_dust", 6)
            .input(Tags.Items.ENDER_PEARLS)
            .input(Tags.Items.DYES_PURPLE)
            .processingTime(12)
            .energyCost(200)
            .build(c);
    }

    private void mekanismGrinderBiofuel(Consumer<? super FinishedRecipe> c, int count, ItemLike... inputs) {
        for (var input : inputs) {
            ProcessorRecipeBuilder
                .grinder("mekanism:bio_fuel", count)
                .input(input)
                .processingTime(15)
                .energyCost(100)
                .build(c, f("/{}", GameUtil.getIdFromItem(input.asItem())));
        }
    }
}
