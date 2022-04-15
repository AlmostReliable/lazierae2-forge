package com.almostreliable.lazierae2.multiblock;

import com.almostreliable.lazierae2.core.Setup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlockEntity extends BlockEntity {

    public ControllerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Setup.BlockEntities.CONTROLLER.get(), blockPos, blockState);
    }

    public boolean isValid() {
        return getBlockState().getValue(ControllerBlock.VALID);
    }

    public void invalidate() {

    }

    public void setMultiBlockData(MultiBlock.Data data) {

    }
}
