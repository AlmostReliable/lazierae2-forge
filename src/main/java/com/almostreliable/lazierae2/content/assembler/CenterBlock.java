package com.almostreliable.lazierae2.content.assembler;

import com.almostreliable.lazierae2.core.TypeEnums.CENTER_TYPE;
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

public class CenterBlock extends AssemblerBlock implements EntityBlock {

    private final CENTER_TYPE type;

    public CenterBlock(CENTER_TYPE type) {
        this.type = type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CenterEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        return openScreen(level, pos, player);
    }

    public CENTER_TYPE getType() {
        return type;
    }
}
