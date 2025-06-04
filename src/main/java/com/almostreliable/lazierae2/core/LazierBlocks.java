package com.almostreliable.lazierae2.core;

import appeng.block.AEBaseBlock;
import appeng.decorative.AEDecorativeBlock;
import com.almostreliable.lazierae2.BuildConfig;
import com.almostreliable.lazierae2.Utils;
import com.almostreliable.lazierae2.block.ResonatingBuddingBlock;
import com.almostreliable.lazierae2.block.ResonatingClusterBlock;
import com.almostreliable.lazierae2.data.LazierLang;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class LazierBlocks {

    static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(BuildConfig.MOD_ID);
    private static final BlockBehaviour.Properties RESONATING_PROPS = AEBaseBlock.stoneProps()
        .strength(3, 5)
        .requiresCorrectToolForDrops();

    // @formatter:off

    // decorative blocks
    public static final DeferredBlock<AEDecorativeBlock> RESONATING_BLOCK = register("resonating_block", () -> new AEDecorativeBlock(RESONATING_PROPS));

    // resonating budding blocks
    public static final DeferredBlock<ResonatingBuddingBlock> DAMAGED_RESONATING_BUDDING_BLOCK = register("damaged_resonating_budding_block", () -> new ResonatingBuddingBlock(RESONATING_PROPS));
    public static final DeferredBlock<ResonatingBuddingBlock> CHIPPED_RESONATING_BUDDING_BLOCK = register("chipped_resonating_budding_block", () -> new ResonatingBuddingBlock(RESONATING_PROPS));
    public static final DeferredBlock<ResonatingBuddingBlock> FLAWED_RESONATING_BUDDING_BLOCK = register("flawed_resonating_budding_block", () -> new ResonatingBuddingBlock(RESONATING_PROPS));

    // resonating buds and crystal cluster
    public static final DeferredBlock<ResonatingClusterBlock> SMALL_RESONATING_BUD = register("small_resonating_bud", () -> new ResonatingClusterBlock(3, 4, RESONATING_PROPS));
    public static final DeferredBlock<ResonatingClusterBlock> MEDIUM_RESONATING_BUD = register("medium_resonating_bud", () -> new ResonatingClusterBlock(4, 3, RESONATING_PROPS));
    public static final DeferredBlock<ResonatingClusterBlock> LARGE_RESONATING_BUD = register("large_resonating_bud", () -> new ResonatingClusterBlock(5, 3, RESONATING_PROPS));
    public static final DeferredBlock<ResonatingClusterBlock> RESONATING_CLUSTER = register("resonating_cluster", () -> new ResonatingClusterBlock(7, 3, RESONATING_PROPS));

    // @formatter:on

    private LazierBlocks() {}

    private static <T extends Block> DeferredBlock<T> register(String id, Supplier<T> blockSupplier, @Nullable String name) {
        var deferredBlock = REGISTRY.register(id, blockSupplier);
        LazierItems.REGISTRY.register(id, () -> new BlockItem(deferredBlock.get(), new Item.Properties()), name);
        LazierLang.LangEntry.of("block", id, Utils.getNameOrFormatId(id, name));
        return deferredBlock;
    }

    private static <T extends Block> DeferredBlock<T> register(String id, Supplier<T> blockSupplier) {
        return register(id, blockSupplier, null);
    }
}
