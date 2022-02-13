package com.github.almostreliable.lazierae2.machine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class MachineBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    private final int inputSlots;

    public MachineBlock(int inputSlots) {
        super(Properties.of(Material.METAL).strength(5f).harvestTool(ToolType.PICKAXE).sound(SoundType.METAL));
        this.inputSlots = inputSlots;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
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

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new MachineTile(inputSlots);
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

    int getInputSlots() {
        return inputSlots;
    }
}
