package com.almostreliable.lazierae2.content;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

    protected void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        var oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(GenericBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(GenericBlock.ACTIVE, state));
        }
    }

    protected abstract void playerDestroy(boolean creative);

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return TextUtil.translate(TRANSLATE_TYPE.BLOCK, getId());
    }

    private String getId() {
        return getBlock().getId();
    }

    private GenericBlock getBlock() {
        return (GenericBlock) getBlockState().getBlock();
    }
}
