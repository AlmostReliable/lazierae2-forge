package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.assembler.MultiBlock.Data;
import com.almostreliable.lazierae2.core.Setup.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ControllerEntity extends BlockEntity {

    private Data data;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Entities.CONTROLLER.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (data != null) {
            tag.put("data", Data.save(data));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("data")) {
            data = Data.load(tag.getCompound("data"));
        }
    }

    public boolean isValid() {
        return getBlockState().getValue(ControllerBlock.CONTROLLER_VALID);
    }

    @Nullable
    public Data getMultiBlockData() {
        return data;
    }

    public void setMultiBlockData(@Nullable Data data) {
        this.data = data;
    }
}
