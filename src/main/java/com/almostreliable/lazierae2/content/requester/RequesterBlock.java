package com.almostreliable.lazierae2.content.requester;

import com.almostreliable.lazierae2.content.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RequesterBlock extends MachineBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RequesterEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
        Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack
    ) {
        var entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof RequesterEntity requester && placer instanceof Player player) {
            requester.setOwner(player);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
