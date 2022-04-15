package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.core.Setup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ControllerBlockEntity extends BlockEntity {

    private MultiBlock.Data data;

    public ControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Setup.BlockEntities.CONTROLLER.get(), blockPos, blockState);
    }

    public boolean isValid() {
        return getBlockState().getValue(ControllerBlock.CONTROLLER_VALID);
    }

    @Nullable
    public MultiBlock.Data getMultiBlockData() {
        return data;
    }

    public void setMultiBlockData(@Nullable MultiBlock.Data data) {
        this.data = data;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (data != null) {
            tag.put("data", MultiBlock.Data.save(data));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("data")) {
            data = MultiBlock.Data.load(tag.getCompound("data"));
        }
    }
}
