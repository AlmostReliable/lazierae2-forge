package com.almostreliable.lazierae.registry;

import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import com.almostreliable.lazierae.Constants;
import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.block.CrystalInjectorBlock;
import com.almostreliable.lazierae.block.MachineBlock;
import com.almostreliable.lazierae.block.ProcessingMachineBlock;
import com.almostreliable.lazierae.block.resonating.BuddingResonatingBlock;
import com.almostreliable.lazierae.block.resonating.ResonatingClusterBlock;
import com.almostreliable.lazierae.data.LazierLootFunctions;
import com.almostreliable.lazierae.data.LazierTemplates;
import com.almostreliable.lazierae.util.EnergyGeneratorBlock;
import com.almostreliable.lib.datagen.template.BlockStateTemplates;
import com.almostreliable.lib.registry.BlockEntry;
import com.almostreliable.lib.registry.BlockRegistration;
import com.almostreliable.lib.util.ToolType;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.decorative.AEDecorativeBlock;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LazierBlocks {

    private static final BlockRegistration REGISTRY = LazierAE2.MANAGER.blocks();

    //@formatter:off

    // decorative
    public static final BlockEntry<AEDecorativeBlock> RESONATING_BLOCK = resonatingBuilder("resonating_crystal_block", Material.STONE, AEDecorativeBlock::new).dropSelf().register();

    // budding resonating blocks
    public static final BlockEntry<BuddingResonatingBlock> DAMAGED_BUDDING_RESONATING = budding("damaged_budding_resonating_crystal", RESONATING_BLOCK::get);
    public static final BlockEntry<BuddingResonatingBlock> CHIPPED_BUDDING_RESONATING = budding("chipped_budding_resonating_crystal", DAMAGED_BUDDING_RESONATING::get);
    public static final BlockEntry<BuddingResonatingBlock> FLAWED_BUDDING_RESONATING = budding("flawed_budding_resonating_crystal", CHIPPED_BUDDING_RESONATING::get);

    // resonating buds and crystal cluster
    public static final BlockEntry<ResonatingClusterBlock> SMALL_RESONATING_BUD = bud("small_resonating_bud", SoundType.SMALL_AMETHYST_BUD, 3, 4, 1);
    public static final BlockEntry<ResonatingClusterBlock> MEDIUM_RESONATING_BUD = bud("medium_resonating_bud", SoundType.MEDIUM_AMETHYST_BUD, 4, 3, 2);
    public static final BlockEntry<ResonatingClusterBlock> LARGE_RESONATING_BUD = bud("large_resonating_bud", SoundType.LARGE_AMETHYST_BUD, 5, 3, 4);
    public static final BlockEntry<ResonatingClusterBlock> RESONATING_CLUSTER = cluster("resonating_crystal_cluster");

    // machines
    public static final BlockEntry<CrystalInjectorBlock> INJECTOR = register(Constants.INJECTOR_ID, CrystalInjectorBlock::new);
    public static final BlockEntry<ProcessingMachineBlock> ETCHER = register(ProcessingMachineType.ETCHER, ProcessingMachineBlock::new);
    public static final BlockEntry<ProcessingMachineBlock> INFUSER = register(ProcessingMachineType.INFUSER, ProcessingMachineBlock::new);

    // dev
    public static final BlockEntry<EnergyGeneratorBlock> ENERGY_GENERATOR = REGISTRY.builder("energy_generator", EnergyGeneratorBlock::new).defaultItem().register();

    //@formatter:on

    private LazierBlocks() {}

    static void init() {
        LazierAE2.LOGGER.info("Initializing blocks");
    }

    private static BlockEntry<BuddingResonatingBlock> budding(String id, Supplier<Block> drop) {
        return resonatingBuilder(id, Material.STONE, BuddingResonatingBlock::new)
            .loot(e -> LazierLootFunctions.singleItemTableWithSilkTouch(drop.get(), RESONATING_BLOCK.get()))
            .randomTicks()
            .register();
    }

    private static BlockEntry<ResonatingClusterBlock> bud(String id, SoundType sound, int i, int j, int light) {
        return clusterLike(id, sound, i, j, light)
            .loot(e -> LazierLootFunctions.singleItemTableWithSilkTouch(e.get(), LazierItems.RESONATING_DUST))
            .register();
    }

    private static BlockEntry<ResonatingClusterBlock> cluster(String id) {
        return clusterLike(id, SoundType.AMETHYST_CLUSTER, 7, 3, 5)
            .loot(e -> LazierLootFunctions.silkTouchDispatchTable(
                e.get(),
                LootItem.lootTableItem(LazierItems.RESONATING_CRYSTAL)
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                    .apply(ApplyExplosionDecay.explosionDecay())
            ))
            .register();
    }

    private static BlockRegistration.Builder<ResonatingClusterBlock> clusterLike(
        String id, SoundType sound, int i, int j, int light
    ) {
        return resonatingBuilder(id, Material.AMETHYST, p -> new ResonatingClusterBlock(i, j, p))
            .blockState(LazierTemplates::crystalClusterBlockState)
            .item(builder -> builder.model(LazierTemplates::simpleItemModelFromBlock), AEBaseBlockItem::new)
            .renderCutout()
            .color(MaterialColor.COLOR_LIGHT_GREEN)
            .sound(sound)
            .strength(1.5f)
            .lightLevel(light);
    }

    private static <B extends Block> BlockRegistration.Builder<B> resonatingBuilder(
        String id, Material material, Function<BlockBehaviour.Properties, B> block
    ) {
        return REGISTRY.builder(id, material, block)
            .blockState(BlockStateTemplates::simple)
            .item($ -> {
            }, AEBaseBlockItem::new)
            .sound(SoundType.STONE)
            .strength(3)
            .explosionResistance(5)
            .requiresCorrectToolForDrops(Tiers.IRON, ToolType.PICKAXE);
    }

    // TODO: replace with manual block props
    private static <B extends Block> BlockEntry<B> register(String id, Function<BlockBehaviour.Properties, B> block) {
        return REGISTRY.builder(id, Material.METAL, p -> block.apply(AEBaseBlock.defaultProps(Material.METAL)))
            .item($ -> {}, AEBaseBlockItem::new)
            .blockState(LazierTemplates::injectorBlockState)
            .effectiveTools(ToolType.PICKAXE)
            .dropSelf()
            .lightLevel(state -> state.getValue(MachineBlock.ACTIVE) ? 2 : 0)
            .register();
    }

    private static <B extends ProcessingMachineBlock> BlockEntry<B> register(
        ProcessingMachineType processingMachineType, BiFunction<BlockBehaviour.Properties, ProcessingMachineType, B> block
    ) {
        return REGISTRY.builder(
                processingMachineType.getId(),
                Material.METAL, p -> block.apply(AEBaseBlock.defaultProps(Material.METAL), processingMachineType)
            )
            .item($ -> {}, AEBaseBlockItem::new)
            .blockState(LazierTemplates::machineBlockState)
            .effectiveTools(ToolType.PICKAXE)
            .dropSelf()
            .lightLevel(state -> state.getValue(MachineBlock.ACTIVE) ? 2 : 0)
            .register();
    }
}
