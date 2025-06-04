package com.almostreliable.lazierae.data;

import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class LazierLootFunctions extends BlockLoot {

    private LazierLootFunctions() {}

    public static LootTable.Builder singleItemTableWithSilkTouch(Block block, ItemLike noSilkTouchItem) {
        return createSingleItemTableWithSilkTouch(block, noSilkTouchItem);
    }

    public static LootTable.Builder silkTouchDispatchTable(
        Block block, LootPoolEntryContainer.Builder<?> alternativeEntryBuilder
    ) {
        return createSilkTouchDispatchTable(block, alternativeEntryBuilder);
    }
}
