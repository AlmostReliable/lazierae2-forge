package com.almostreliable.lazierae2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class AbstractValidBlock extends Block {
    public static final OptionalDirectionProperty CTRL_HORIZONTAL = OptionalDirectionProperty.HORIZONTAL;
    public static final OptionalDirectionProperty CTRL_VERTICAL = OptionalDirectionProperty.VERTICAL;

    public AbstractValidBlock(Properties props) {
        super(props);
        this.registerDefaultState(this
            .getStateDefinition()
            .any()
            .setValue(CTRL_HORIZONTAL, OptionalDirection.NONE)
            .setValue(CTRL_VERTICAL, OptionalDirection.NONE));
    }

    @Override
    public InteractionResult use(
        BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return super.use(blockState, level, blockPos, player, hand, hit);
        }

        OptionalDirection horizontalDirection = blockState.getValue(CTRL_HORIZONTAL);
        OptionalDirection verticalDirection = blockState.getValue(CTRL_VERTICAL);

        BlockState foundState = findControllerBlockState(level, blockPos, horizontalDirection, verticalDirection);
        System.out.println(foundState);
        if(foundState != null) {
            player.sendMessage(new TextComponent("Controller found " + foundState),player.getUUID());
        }

        return InteractionResult.CONSUME;
    }

    @Nullable
    public BlockState findControllerBlockState(
        Level level, BlockPos blockPos, OptionalDirection horizontalDirection, OptionalDirection verticalDirection
    ) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for (int i = 0; i < ControllerBlock.MAX_SIZE; i++) {
            horizontalDirection.relative(mutable);
            verticalDirection.relative(mutable);

            BlockState relativeBlockState = level.getBlockState(mutable);
            System.out.println(blockPos + " | " + relativeBlockState);
            if (relativeBlockState.getBlock() instanceof ControllerBlock) {
                return relativeBlockState;
            }

            if (relativeBlockState.getBlock() instanceof AbstractValidBlock) {
                OptionalDirection horizontal = relativeBlockState.getValue(CTRL_HORIZONTAL);
                OptionalDirection vertical = relativeBlockState.getValue(CTRL_VERTICAL);
                return findControllerBlockState(level, mutable, horizontal, vertical);
            }
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CTRL_HORIZONTAL, CTRL_VERTICAL);
    }
}
