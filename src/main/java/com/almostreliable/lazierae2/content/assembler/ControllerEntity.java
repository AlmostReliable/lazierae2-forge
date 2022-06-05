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

import static com.almostreliable.lazierae2.core.Constants.Nbt.DATA_ID;

public class ControllerEntity extends GenericEntity {

    @Nullable
    private MultiBlockData data;

    public ControllerEntity(BlockPos pos, BlockState state) {
        super(Assembler.ASSEMBLER_CONTROLLER.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(DATA_ID)) {
            data = MultiBlockData.load(tag.getCompound(DATA_ID));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (data != null) {
            tag.put(DATA_ID, MultiBlockData.save(data));
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
    MultiBlockData getData() {
        return data;
    }

    void setData(@Nullable MultiBlockData data) {
        this.data = data;
    }
}
