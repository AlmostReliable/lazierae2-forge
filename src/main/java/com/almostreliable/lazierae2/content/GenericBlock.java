package com.almostreliable.lazierae2.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public abstract class GenericBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    protected GenericBlock() {
        super(Properties.of(Material.METAL).strength(5f).sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(ACTIVE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        var entity = level.getBlockEntity(pos);
        if (entity instanceof MenuProvider menuProvider && player instanceof ServerPlayer invoker) {
            NetworkHooks.openGui(invoker, menuProvider, pos);
        }
        return InteractionResult.CONSUME;
    }
}
