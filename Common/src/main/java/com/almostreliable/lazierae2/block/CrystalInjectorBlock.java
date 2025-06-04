package com.almostreliable.lazierae2.block;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import com.almostreliable.lazierae2.blockentity.CrystalInjectorBlockEntity;
import com.almostreliable.lazierae2.registry.LazierMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class CrystalInjectorBlock extends AEBaseEntityBlock<CrystalInjectorBlockEntity> {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public CrystalInjectorBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return defaultBlockState().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult onActivated(
        Level level, BlockPos pos, Player player, InteractionHand hand, @Nullable ItemStack item, BlockHitResult hit
    ) {
        if (!InteractionUtil.isInAlternateUseMode(player)) {
            CrystalInjectorBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                if (!level.isClientSide()) {
                    MenuOpener.open(LazierMenus.INJECTOR.get(), player, MenuLocators.forBlockEntity(blockEntity));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }
}
