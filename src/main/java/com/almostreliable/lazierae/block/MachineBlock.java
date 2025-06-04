package com.almostreliable.lazierae.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import com.almostreliable.lib.AlmostLib;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.InteractionUtil;

import org.jetbrains.annotations.Nullable;

public abstract class MachineBlock<BE extends AEBaseBlockEntity & MenuProvider> extends AEBaseEntityBlock<BE> {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected MachineBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult onActivated(
        Level level, BlockPos pos, Player player, InteractionHand hand, @Nullable ItemStack heldItem, BlockHitResult hit
    ) {
        if (InteractionUtil.isInAlternateUseMode(player)) return InteractionResult.PASS;

        BE blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null) return InteractionResult.PASS;

        if (!level.isClientSide) {
            AlmostLib.PLATFORM.openMenu((ServerPlayer) player, blockEntity, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
