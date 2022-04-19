package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.content.assembler.MultiBlock.Data;
import com.almostreliable.lazierae2.core.Setup.Entities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ControllerEntity extends GenericEntity {

    private Data data;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Entities.ASSEMBLER_CONTROLLER.get(), pos, state);
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return null;
    }

    public boolean isMultiBlockMaster() {
        return getBlockState().getValue(ControllerBlock.IS_MULTIBLOCK);
    }

    @Nullable
    public Data getMultiBlockData() {
        return data;
    }

    public void setMultiBlockData(@Nullable Data data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return null;
    }
}
