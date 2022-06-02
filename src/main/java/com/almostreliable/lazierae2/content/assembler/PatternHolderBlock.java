package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.content.GenericBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class PatternHolderBlock extends GenericBlock implements EntityBlock {

    private final HOLDER_TIER tier;

    public PatternHolderBlock(HOLDER_TIER tier) {
        this.tier = tier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (tier == HOLDER_TIER.ACCELERATOR) return null;
        return new PatternHolderEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        return openScreen(level, pos, player);
    }

    HOLDER_TIER getTier() {
        return tier;
    }

    public enum HOLDER_TIER {
        TIER_1, TIER_2, TIER_3, ACCELERATOR
    }
}
