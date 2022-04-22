package com.almostreliable.lazierae2.content;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class GenericBlock extends Block {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected GenericBlock() {
        super(Properties.of(Material.METAL).strength(5f).sound(SoundType.METAL));
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var superState = super.getStateForPlacement(context);
        var state = superState == null ? defaultBlockState() : superState;
        return state.setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public void appendHoverText(
        ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag
    ) {
        var description = TextUtil.translateAsString(TRANSLATE_TYPE.TOOLTIP, f("{}.description", getId()));
        if (!description.isEmpty()) {
            tooltip.addAll(Tooltip.builder().line(f("{}.description", getId()), ChatFormatting.AQUA).build());
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    protected InteractionResult openScreen(
        Level level, BlockPos pos, Player player
    ) {
        if (level.isClientSide() || player.isShiftKeyDown()) return InteractionResult.SUCCESS;
        var entity = level.getBlockEntity(pos);
        if (entity instanceof MenuProvider menuProvider && player instanceof ServerPlayer invoker) {
            NetworkHooks.openGui(invoker, menuProvider, pos);
        }
        return InteractionResult.CONSUME;
    }

    public String getId() {
        var registryName = getRegistryName();
        assert registryName != null;
        return registryName.getPath();
    }
}
