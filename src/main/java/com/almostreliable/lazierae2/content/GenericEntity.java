package com.almostreliable.lazierae2.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GenericEntity extends BlockEntity implements MenuProvider {

    protected GenericEntity(
        BlockEntityType<?> type, BlockPos pos, BlockState state
    ) {
        super(type, pos, state);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    protected void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(GenericBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(GenericBlock.ACTIVE, state));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
}
