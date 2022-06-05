package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.MultiBlockData;
import com.almostreliable.lazierae2.core.Setup.Entities.Assembler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ControllerEntity extends GenericEntity {

    @Nullable
    private MultiBlockData multiBlockData;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Assembler.ASSEMBLER_CONTROLLER.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (multiBlockData != null) {
            tag.put("data", MultiBlockData.save(multiBlockData));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("data")) {
            multiBlockData = MultiBlockData.load(tag.getCompound("data"));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return null;
    }

    @Override
    protected void playerDestroy(boolean creative) {
        // TODO: implement
    }

    @Nullable
    MultiBlockData getMultiBlockData() {
        return multiBlockData;
    }

    void setMultiBlockData(@Nullable MultiBlockData data) {
        multiBlockData = data;
    }
}
