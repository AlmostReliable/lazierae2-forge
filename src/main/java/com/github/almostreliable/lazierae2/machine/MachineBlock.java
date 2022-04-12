package com.github.almostreliable.lazierae2.machine;

import com.github.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.github.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class MachineBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    private final MachineType machineType;

    public MachineBlock(MachineType machineType) {
        super(Properties.of(Material.METAL).strength(5f).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
        this.machineType = machineType;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(ACTIVE, false);
    }

    @Override
    public void setPlacedBy(
        World level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack
    ) {
        TileEntity tile = level.getBlockEntity(pos);
        if (!level.isClientSide && tile instanceof MachineTile) {
            MachineTile machine = (MachineTile) tile;
            machine.playerPlace(stack);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void playerWillDestroy(World level, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tile = level.getBlockEntity(pos);
        if (!level.isClientSide && tile instanceof MachineTile) {
            MachineTile machine = (MachineTile) tile;
            if (!player.isCreative()) machine.playerDestroy();
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
        ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag
    ) {
        CompoundNBT nbt = stack.getTag();
        int upgrades = 0;
        int energy = 0;
        boolean sideConfig = false;
        if (nbt != null) {
            if (nbt.contains(UPGRADES_ID)) upgrades = ItemStack.of(nbt.getCompound(UPGRADES_ID)).getCount();
            if (nbt.contains(ENERGY_ID)) energy = nbt.getCompound(ENERGY_ID).getInt(ENERGY_ID);
            if (nbt.contains(SIDE_CONFIG_ID)) sideConfig = true;
        }

        if (upgrades > 0 || energy > 0 || sideConfig) {
            int finalUpgrades = upgrades;
            int finalEnergy = energy;
            boolean finalSideConfig = sideConfig;
            tooltip.addAll(Tooltip
                .builder()
                .keyValue("item.upgrades", () -> finalUpgrades, machineType::getUpgradeSlots)
                .keyValue("item.energy", () -> TextUtil.formatEnergy(finalEnergy, 1, 3, Screen.hasShiftDown(), true))
                .line(() -> finalSideConfig, "item.side_config", TextFormatting.YELLOW)
                .build());
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MachineTile(machineType.getInputSlots(), machineType.getBaseEnergyBuffer());
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType use(
        BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit
    ) {
        if (level.isClientSide() || player.isShiftKeyDown()) return ActionResultType.SUCCESS;
        TileEntity tile = level.getBlockEntity(pos);
        if (tile instanceof INamedContainerProvider && player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tile, pos);
        }
        return ActionResultType.CONSUME;
    }

    public String getId() {
        ResourceLocation registryName = getRegistryName();
        assert registryName != null;
        return registryName.getPath();
    }

    MachineType getMachineType() {
        return machineType;
    }
}
