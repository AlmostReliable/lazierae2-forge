package com.almostreliable.lazierae2.machine;

import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.*;

public class MachineBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    private final MachineType machineType;

    // TODO: implement harvest tool
    public MachineBlock(MachineType machineType) {
        super(Properties.of(Material.METAL).strength(5f).sound(SoundType.METAL));
        this.machineType = machineType;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(ACTIVE, false);
    }

    @Override
    public void setPlacedBy(
        Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack
    ) {
        var entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof MachineEntity machine) {
            machine.playerPlace(stack);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        var entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof MachineEntity machine && !player.isCreative()) {
            machine.playerDestroy();
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(ACTIVE);
    }

    @Override
    public void appendHoverText(
        ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag
    ) {
        var tag = stack.getTag();
        var upgrades = 0;
        var energy = 0;
        var sideConfig = false;
        if (tag != null) {
            if (tag.contains(UPGRADES_ID)) upgrades = ItemStack.of(tag.getCompound(UPGRADES_ID)).getCount();
            if (tag.contains(ENERGY_ID)) energy = tag.getCompound(ENERGY_ID).getInt(ENERGY_ID);
            if (tag.contains(SIDE_CONFIG_ID)) sideConfig = true;
        }

        if (upgrades > 0 || energy > 0 || sideConfig) {
            var finalUpgrades = upgrades;
            var finalEnergy = energy;
            var finalSideConfig = sideConfig;
            tooltip.addAll(Tooltip
                .builder()
                .keyValue("item.upgrades", () -> finalUpgrades, machineType::getUpgradeSlots)
                .keyValue("item.energy", () -> TextUtil.formatEnergy(finalEnergy, 1, 3, Screen.hasShiftDown(), true))
                .line(() -> finalSideConfig, "item.side_config", ChatFormatting.YELLOW)
                .build());
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide) return null;
        return (pLevel, pState, pBlockEntityType, pBlockEntity) -> {
            if (pBlockEntity instanceof MachineEntity machine) {
                machine.tick();
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(
        BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        var entity = level.getBlockEntity(pos);
        if (entity instanceof MenuProvider machine && player instanceof ServerPlayer invoker) {
            NetworkHooks.openGui(invoker, machine, pos);
        }
        return InteractionResult.CONSUME;
    }

    public String getId() {
        var registryName = getRegistryName();
        assert registryName != null;
        return registryName.getPath();
    }

    MachineType getMachineType() {
        return machineType;
    }
}
