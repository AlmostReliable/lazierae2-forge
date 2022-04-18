package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.*;

public class ProcessorBlock extends GenericBlock {

    private final ProcessorType processorType;

    public ProcessorBlock(ProcessorType processorType) {
        this.processorType = processorType;
    }

    @Override
    public void setPlacedBy(
        Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack
    ) {
        var entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof ProcessorEntity processor) {
            processor.playerPlace(stack);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        var entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof ProcessorEntity processor && !player.isCreative()) {
            processor.playerDestroy();
        }
        super.playerWillDestroy(level, pos, state, player);
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
                .keyValue("item.upgrades", () -> finalUpgrades, processorType::getUpgradeSlots)
                .keyValue("item.energy", () -> TextUtil.formatEnergy(finalEnergy, 1, 3, Screen.hasShiftDown(), true))
                .line(() -> finalSideConfig, "item.side_config", ChatFormatting.YELLOW)
                .build());
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProcessorEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide) return null;
        return (entityLevel, entityState, entityType, entity) -> {
            if (entity instanceof ProcessorEntity processor) {
                processor.tick();
            }
        };
    }

    public String getId() {
        var registryName = getRegistryName();
        assert registryName != null;
        return registryName.getPath();
    }

    ProcessorType getProcessorType() {
        return processorType;
    }
}
