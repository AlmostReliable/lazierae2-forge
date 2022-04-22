package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.component.InventoryHandler.PatternInventory;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Setup.Entities.Assembler;
import com.almostreliable.lazierae2.core.TypeEnums.CENTER_TYPE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CenterEntity extends GenericEntity {

    private final PatternInventory patternStorage;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public CenterEntity(
        BlockPos pos, BlockState state
    ) {
        super(Assembler.CENTER.get(), pos, state);
        patternStorage = new PatternInventory(this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return null;
    }

    public CENTER_TYPE getProcessorType() {
        return ((CenterBlock) getBlockState().getBlock()).getType();
    }
}
