package com.almostreliable.lazierae.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.block.ProcessingMachineBlock;
import com.almostreliable.lazierae.inventory.SidedInventoryHost;
import com.almostreliable.lazierae.recipe.ProcessingMachineRecipe;
import com.almostreliable.lazierae.registry.LazierBlockEntities;
import com.almostreliable.lazierae.registry.ProcessingMachineType;
import com.almostreliable.lib.block.BlockSide;
import com.almostreliable.lib.block.io.SideConfiguration;
import com.almostreliable.lib.component.EnergyContainer;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ProcessingMachineBlockEntity extends MachineBlockEntity
    implements SidedInventoryHost, RecipeProcessor<ProcessingMachineRecipe> {

    private final SideConfiguration sideConfig;
    private final RecipeProcessContainer<ProcessingMachineRecipe> recipeContainer;

    public ProcessingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(LazierBlockEntities.PROCESSING_MACHINE.get(), pos, state);
        sideConfig = new SideConfiguration(this);
        recipeContainer = new RecipeProcessContainer<>();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.put("side_config", sideConfig.serialize());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("side_config")) sideConfig.deserialize(data.getCompound("side_config"));
    }

    @Override
    public Direction getFacing() {
        return getForward();
    }

    @Nullable
    @Override
    public Direction getTop() {
        return getUp();
    }

    @Override
    public boolean allowsConfiguration(Direction dir, BlockSide side) {
        return dir != getFacing();
    }

    @Override
    protected void updateExposedNodeSides() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(getFacing());
        getMainNode().setExposedOnSides(exposedSides);
    }

    @Nullable
    @Override
    public Container getItemContainer(@Nullable Direction side) {
        if (side == null || sideConfig.get(side).isConfigured()) return inventory;
        return null;
    }

    @Nullable
    @Override
    public EnergyContainer getEnergyContainer(@Nullable Direction side) {
        if (side == getFacing()) return null;
        return energy;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return handleTickingRequest();
    }

    @Override
    public void onInventoryChange(int slot, ItemStack stack) {

    }

    @Override
    public void onUpgradeChange(int amount) {

    }

    @Override
    public void onEnergyChange() {

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public TickRateModulation findAndProcessRecipe() {
        return null;
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return getProcessingMachineType().getBlockEntry().asItem();
    }

    @Override
    public TickRateModulation processRecipe() {
        return null;
    }

    @Override
    public RecipeProcessContainer<ProcessingMachineRecipe> getRecipeContainer() {
        return recipeContainer;
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public SideConfiguration getSideConfiguration() {
        return sideConfig;
    }

    public ProcessingMachineType getProcessingMachineType() {
        return ((ProcessingMachineBlock) getBlockState().getBlock()).getProcessingMachineType();
    }
}
