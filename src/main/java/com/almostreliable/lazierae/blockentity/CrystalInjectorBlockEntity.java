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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.LazierAE2;
import com.almostreliable.lazierae.data.LazierLang;
import com.almostreliable.lazierae.menu.CrystalInjectorMenu;
import com.almostreliable.lazierae.recipe.CrystalInjectorRecipe;
import com.almostreliable.lazierae.registry.LazierBlockEntities;
import com.almostreliable.lazierae.registry.LazierBlocks;
import com.almostreliable.lib.block.BlockSide;
import com.almostreliable.lib.component.EnergyContainer;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class CrystalInjectorBlockEntity extends MachineBlockEntity {

    /*
        TODO:
        - add gui button to toggle between energy modes:
          - default mode: accept FE and AE2 power (only push based) but prefer FE
          - accept FE and AE2 power (actively requesting) but prefer FE
          - FE only
        - add gui button to allow network export of outputs if connected to a network
        - add button to cancel current recipe (drop buffered input)
     */

    private final BlockPos targetPos;

    @Nullable
    private CrystalInjectorRecipe recipe;
    private ItemStack bufferedInput = ItemStack.EMPTY;
    private int recipeTime;
    private int recipeProgress;

    public CrystalInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(LazierBlockEntities.INJECTOR.get(), pos, state);
        this.targetPos = pos.above();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        if (recipe != null) {
            data.put("buffered_input", bufferedInput.serialize());
            data.putInt("recipe_time", recipeTime);
            data.putInt("recipe_progress", recipeProgress);
        }
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("buffered_input")) bufferedInput = ItemStack.of(data.getCompound("buffered_input"));
        if (data.contains("recipe_time")) recipeTime = data.getInt("recipe_time");
        if (data.contains("recipe_progress")) recipeProgress = data.getInt("recipe_progress");
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return LazierBlocks.INJECTOR.asItem();
    }

    @Override
    public Component getDisplayName() {
        return LazierLang.INJECTOR.get();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int wid, Inventory inventory, Player player) {
        return new CrystalInjectorMenu(wid, inventory, this);
    }

    @Override
    public void onInventoryChange(int slot, ItemStack stack) {
        if (level == null || level.isClientSide) return;
        resetRecipe(); // TODO: only reset if nothing is processing because inputs are buffered anyways
        if (inventory.getItem(0).isEmpty()) return;
        wakeUp();
    }

    @Override
    public void onUpgradeChange(int amount) {

    }

    @Nullable
    @Override
    public Container getItemContainer(@Nullable Direction side) {
        if (side == Direction.UP) return null;
        return inventory;
    }

    @Nullable
    @Override
    public EnergyContainer getEnergyContainer(@Nullable Direction side) {
        if (side == Direction.UP) return null;
        return energy;
    }

    @Override
    public boolean allowsConfiguration(Direction dir, BlockSide side) {
        return dir != Direction.UP;
    }

    @Override
    public boolean allowsOutput(Direction dir, BlockSide side) {
        return false;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (level == null || level.isClientSide) return TickRateModulation.SLEEP;

        if (recipe != null) return processRecipe();
        return findAndProcessRecipe();
    }

    private TickRateModulation findAndProcessRecipe() {
        assert level != null;

        if (level.isEmptyBlock(targetPos)) {
            currentState = MachineState.NO_BLOCK;
            return TickRateModulation.SLEEP;
        }

        ItemStack inputItem = inventory.getItem(0);
        if (inputItem.isEmpty()) {
            currentState = MachineState.NO_ITEM;
            return TickRateModulation.SLEEP;
        }

        recipe = CrystalInjectorRecipe.findRecipe(level, inputItem, targetPos);
        if (recipe == null) {
            currentState = MachineState.NO_RECIPE;
            return TickRateModulation.SLEEP;
        }

        recipeTime = recipe.getTime();

        return processRecipe();
    }

    private TickRateModulation processRecipe() {
        assert level != null && recipe != null;

        long requiredEnergy = calculateRequiredEnergy();
        // if not enough energy for the operation, send node to sleep
        if (energy.extract(requiredEnergy, true) < requiredEnergy) {
            currentState = MachineState.NO_ENERGY;
            return TickRateModulation.SLEEP;
        }

        currentState = MachineState.OK;

        if (bufferedInput.isEmpty()) {
            ItemStack inputItem = inventory.getItem(0);
            int count = recipe.getInput().getCount();
            bufferedInput = inputItem.withCount(count);
            inputItem.shrink(count);
        }

        if (recipeProgress < recipeTime) {
            energy.extract(requiredEnergy, false);
            recipeProgress++;
            saveChanges();
            return TickRateModulation.SAME;
        }

        bufferedInput = ItemStack.EMPTY;
        BlockState outputBlock = recipe.getOutputBlock().decomposeBlockStates().values().iterator().next();
        level.setBlockAndUpdate(targetPos, outputBlock);
        // reset is handled by onNeighborChange

        return TickRateModulation.SAME;
    }

    private long calculateRequiredEnergy() {
        int upgradeCount = upgrades.getUpgradeCount();
        List<Double> energyValues = LazierAE2.CONFIG.get().injector.energyValues;
        double energyMultiplier = upgradeCount == 0 ? 1 : energyValues.get(upgradeCount - 1);

        return (long) (energyMultiplier * Objects.requireNonNull(recipe).getEnergy());
    }

    private int calculateProgressStep() {
        int upgradeCount = upgrades.getUpgradeCount();
        List<Double> speedValues = LazierAE2.CONFIG.get().injector.speedValues;
        double speedMultiplier = upgradeCount == 0 ? 1 : speedValues.get(upgradeCount - 1);

        return (int) (speedMultiplier * Objects.requireNonNull(recipe).getTime());
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (!bufferedInput.isEmpty()) drops.add(bufferedInput);
    }

    @Override
    protected void updateExposedNodeSides() {
        var exposedSides = EnumSet.allOf(Direction.class);
        exposedSides.remove(Direction.UP);
        getMainNode().setExposedOnSides(exposedSides);
    }

    @Override
    public void onEnergyChange() {
        if (level == null || level.isClientSide) return;

        saveChanges();
        if (recipe == null) return;
        wakeUp();
    }

    public void onNeighborChange() {
        if (level == null || level.isClientSide) return;

        boolean nowAir = level.isEmptyBlock(targetPos);

        if (recipe != null && (nowAir || !CrystalInjectorRecipe.testBlock(level, targetPos, recipe.getInputBlock()))) {
            resetRecipe();
            return;
        }

        if (recipe == null && nowAir) return;

        wakeUp();
    }

    private void resetRecipe() {
        recipe = null;
        recipeTime = 0;
        recipeProgress = 0;
        currentState = MachineState.OK;
        saveChanges();
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    public int getRecipeProgress() {
        return recipeProgress;
    }

    @Override
    public Direction getFacing() {
        return Direction.NORTH;
    }
}
