package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.PatternHolderBlock.HOLDER_TIER;
import com.almostreliable.lazierae2.core.Setup.Entities.Assembler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PatternHolderEntity extends GenericEntity {

    private final PatternInventory patternStorage;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public PatternHolderEntity(
        BlockPos pos, BlockState state
    ) {
        super(Assembler.PATTERN_HOLDER.get(), pos, state);
        patternStorage = new PatternInventory(this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return null;
    }

    @Override
    protected void playerDestroy(boolean creative) {
        // TODO: implement
    }

    public HOLDER_TIER getTier() {
        return ((PatternHolderBlock) getBlockState().getBlock()).getTier();
    }
}
