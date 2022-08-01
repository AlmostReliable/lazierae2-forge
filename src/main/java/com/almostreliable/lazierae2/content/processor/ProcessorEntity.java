package com.almostreliable.lazierae2.content.processor;

import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Setup.Entities;
import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.almostreliable.lazierae2.recipe.type.ProcessorRecipe;
import com.almostreliable.lazierae2.util.GameUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntToDoubleFunction;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class ProcessorEntity extends GenericEntity {

    private static final int AUTO_EXTRACT_RATE = 10;
    public final SideConfiguration sideConfig;
    public final EnergyHandler energy;
    private final ProcessorInventory inventory;
    private final LazyOptional<ProcessorInventory> inventoryCap;
    private final LazyOptional<EnergyHandler> energyCap;
    private final Map<Direction, LazyOptional<IItemHandler>> autoExtractCache = new EnumMap<>(Direction.class);
    private ProcessorRecipe recipeCache;
    private boolean autoExtract;
    private int progress;
    private int energyCost;
    private int processTime;
    private int recipeTime;
    private int recipeEnergy;
    private int recipeMultiplier;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public ProcessorEntity(BlockPos pos, BlockState state) {
        super(Entities.PROCESSOR.get(), pos, state);
        inventory = new ProcessorInventory(this);
        inventoryCap = LazyOptional.of(() -> inventory);
        energy = new EnergyHandler(this);
        energyCap = LazyOptional.of(() -> energy);
        sideConfig = new SideConfiguration(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(INVENTORY_ID)) inventory.deserializeNBT(tag.getCompound(INVENTORY_ID));
        if (tag.contains(ENERGY_ID)) energy.deserializeNBT(tag.getCompound(ENERGY_ID));
        if (tag.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(tag.getCompound(SIDE_CONFIG_ID));
        if (tag.contains(AUTO_EXTRACT_ID)) autoExtract = tag.getBoolean(AUTO_EXTRACT_ID);
        if (tag.contains(PROGRESS_ID)) progress = tag.getInt(PROGRESS_ID);
        if (tag.contains(PROCESS_TIME_ID)) processTime = tag.getInt(PROCESS_TIME_ID);
        if (tag.contains(RECIPE_TIME_ID)) recipeTime = tag.getInt(RECIPE_TIME_ID);
        if (tag.contains(ENERGY_COST_ID)) energyCost = tag.getInt(ENERGY_COST_ID);
        if (tag.contains(RECIPE_ENERGY_ID)) recipeEnergy = tag.getInt(RECIPE_ENERGY_ID);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(INVENTORY_ID, inventory.serializeNBT());
        tag.put(ENERGY_ID, energy.serializeNBT());
        tag.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        tag.putBoolean(AUTO_EXTRACT_ID, autoExtract);
        tag.putInt(PROGRESS_ID, progress);
        tag.putInt(PROCESS_TIME_ID, processTime);
        tag.putInt(RECIPE_TIME_ID, recipeTime);
        tag.putInt(ENERGY_COST_ID, energyCost);
        tag.putInt(RECIPE_ENERGY_ID, recipeEnergy);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
        int menuID, Inventory inventory, Player player
    ) {
        return new ProcessorMenu(menuID, this, inventory);
    }

    public void recalculateEnergyCapacity() {
        if (level == null || level.isClientSide) return;
        var baseBuffer = getProcessorType().getBaseEnergyBuffer();
        var upgradeBuffer = getProcessorType().getEnergyBufferAdd();
        var newCapacity = baseBuffer + upgradeBuffer * inventory.getUpgradeCount();
        if (newCapacity != energy.getMaxEnergyStored()) energy.setCapacity(newCapacity);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventory.invalidate();
        inventoryCap.invalidate();
        energyCap.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(
        @Nonnull Capability<T> cap, @Nullable Direction direction
    ) {
        if (!remove) {
            if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)) {
                if (direction == null) return inventoryCap.cast();
                var setting = sideConfig.get(direction);
                if (setting != IO_SETTING.OFF) return inventory.getInventoryCap(setting).cast();
            } else if (cap.equals(CapabilityEnergy.ENERGY)) {
                return energyCap.cast();
            }
        }
        return super.getCapability(cap, direction);
    }

    public double calculateMultiplier(IntToDoubleFunction multiplierList) {
        var upgradeCount = inventory.getUpgradeCount();
        return upgradeCount == 0 ? 1.0 : multiplierList.applyAsDouble(upgradeCount);
    }

    public void insertUpgrades(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var maxUpgrades = getProcessorType().getUpgradeSlots();
        var currentUpgrades = inventory.getUpgradeCount();
        var inserted = Math.min(stack.getCount(), maxUpgrades - currentUpgrades);
        if (inserted > 0) {
            inventory.setStackInSlot(
                ProcessorInventory.UPGRADE_SLOT,
                AEItems.SPEED_CARD.stack(currentUpgrades + inserted)
            );
            stack.shrink(inserted);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            } else {
                player.setItemInHand(hand, stack);
            }
        }
    }

    @Override
    protected void playerDestroy(boolean creative) {
        assert level != null;
        inventory.dropContents(creative);
        if (creative) return;
        var tag = new CompoundTag();
        if (inventory.getUpgradeCount() > 0) tag.put(UPGRADES_ID, inventory.serializeUpgrades());
        if (energy.getEnergyStored() > 0) tag.put(ENERGY_ID, energy.serializeNBT());
        if (sideConfig.isConfigured()) tag.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        if (autoExtract) tag.putBoolean(AUTO_EXTRACT_ID, true);
        var stack = new ItemStack(getProcessorType().getItemProvider());
        if (!tag.isEmpty()) stack.setTag(tag);
        level.addFreshEntity(new ItemEntity(
            level,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            stack
        ));
    }

    void tick() {
        if (level == null || level.isClientSide) return;
        if (autoExtract && level.getGameTime() % AUTO_EXTRACT_RATE == 0) autoExtract();

        var recipe = getRecipe();
        if (recipe == null) return;
        recipeEnergy = recipe.getEnergyCost();
        recipeTime = recipe.getProcessTime();
        var recipeInputSlots = getInputSlotsForRecipe(recipe);

        var energyCostExact = calculateEnergyCost(recipe);
        energyCost = (int) energyCostExact;
        var processTimeExact = calculateProcessTime(recipe);
        processTime = processTimeExact < 1 ? 1 : (int) Math.ceil(processTimeExact);
        recipeMultiplier = processTimeExact <= 0.5 ? (int) (1 / processTimeExact) : 1;

        if (canWork(recipe, energyCostExact, recipeInputSlots)) {
            doWork(energyCostExact);
        } else {
            changeActivityState(false);
        }
        if (progress >= processTime) finishWork(recipe, recipeInputSlots);
    }

    void playerPlace(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return;
        if (tag.contains(UPGRADES_ID)) inventory.deserializeUpgrades(tag.getCompound(UPGRADES_ID));
        if (tag.contains(ENERGY_ID)) energy.deserializeNBT(tag.getCompound(ENERGY_ID));
        if (tag.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(tag.getCompound(SIDE_CONFIG_ID));
        if (tag.contains(AUTO_EXTRACT_ID)) autoExtract = tag.getBoolean(AUTO_EXTRACT_ID);
    }

    private void finishWork(ProcessorRecipe recipe, Map<Integer, Integer> recipeInputSlots) {
        inventory.shrinkInputSlots(recipeInputSlots, recipeMultiplier);
        if (inventory.getStackInOutput().isEmpty()) {
            var outputStack = recipe.assemble(inventory.toContainer());
            outputStack.setCount(outputStack.getCount() * recipeMultiplier);
            inventory.setStackInOutput(outputStack);
        } else {
            inventory.getStackInOutput().grow(recipe.getResultItem().getCount() * recipeMultiplier);
        }

        progress = 0;
        setChanged();

        if (processTime < AUTO_EXTRACT_RATE) {
            var produced = recipe.getResultItem().getCount() * recipeMultiplier;
            if (produced + inventory.getStackInOutput()
                .getCount() > inventory.getStackLimit(ProcessorInventory.OUTPUT_SLOT, recipe.getResultItem())) {
                autoExtract();
            }
        }
    }

    private void doWork(double energyCostExact) {
        changeActivityState(true);
        energy.setEnergy((int) (energy.getEnergyStored() - Math.round(energyCostExact * recipeMultiplier / processTime)));
        progress++;
        setChanged();
    }

    private boolean canWork(ProcessorRecipe recipe, double energyCostExact, Map<Integer, Integer> recipeInputSlots) {
        recipeMultiplierByEnergy(energyCostExact);
        if (recipeMultiplier == 0) return false;

        var outputStack = inventory.getStackInOutput();
        var recipeResult = recipe.getResultItem();
        if (!outputStack.isEmpty() && !outputStack.sameItem(recipeResult)) return false;

        var maxOutputSpace = outputStack.isEmpty() ? inventory.getSlotLimit(ProcessorInventory.OUTPUT_SLOT) :
            inventory.getStackLimit(ProcessorInventory.OUTPUT_SLOT, recipeResult) - outputStack.getCount();
        recipeMultiplierByOutput(maxOutputSpace, recipeResult.getCount());
        if (recipeMultiplier == 0) return false;

        var smallestInputCount = -1;
        for (var input : recipeInputSlots.entrySet()) {
            var count = inventory.getStackInSlot(input.getKey()).getCount() / input.getValue();
            if (smallestInputCount == -1 || count < smallestInputCount) {
                smallestInputCount = count;
            }
        }
        if (smallestInputCount == -1) throw new IllegalStateException("No slots to shrink");
        recipeMultiplier = Math.min(recipeMultiplier, smallestInputCount);
        return recipeMultiplier != 0;
    }

    @NotNull
    private Map<Integer, Integer> getInputSlotsForRecipe(ProcessorRecipe recipe) {
        Map<Integer, Integer> slotsToShrink = new HashMap<>();
        for (var input : recipe.getInputs()) {
            for (var slot = ProcessorInventory.NON_INPUT_SLOTS; slot < inventory.getSlots(); slot++) {
                if (input.ingredient().test(inventory.getStackInSlot(slot))) {
                    slotsToShrink.put(slot, input.count());
                    break;
                }
            }
        }
        return slotsToShrink;
    }

    private void recipeMultiplierByEnergy(double energyCostExact) {
        if (recipeMultiplier == 0) return;
        if (energyCostExact * recipeMultiplier / processTime <= energy.getEnergyStored()) return;
        recipeMultiplier--;
        recipeMultiplierByEnergy(energyCostExact);
    }

    private void recipeMultiplierByOutput(int maxOutputSpace, int resultCount) {
        if (recipeMultiplier == 0) return;
        var outputCount = resultCount * recipeMultiplier;
        if (outputCount <= maxOutputSpace) return;
        recipeMultiplier--;
        recipeMultiplierByOutput(maxOutputSpace, resultCount);
    }

    private void stopWork() {
        changeActivityState(false);
        progress = 0;
        recipeCache = null;
    }

    private double calculateEnergyCost(ProcessorRecipe recipe) {
        var baseCost = recipe.getEnergyCost();
        return baseCost * calculateMultiplier(upgrades -> getProcessorType().getEnergyCostMultiplier(upgrades));
    }

    private double calculateProcessTime(ProcessorRecipe recipe) {
        var baseTime = recipe.getProcessTime();
        return baseTime * calculateMultiplier(upgrades -> getProcessorType().getProcessTimeMultiplier(upgrades));
    }

    private void autoExtract() {
        assert level != null;
        if (inventory.getStackInOutput().isEmpty()) return;

        sideConfig.forEachOutput(direction -> {
            var extractEntity = level.getBlockEntity(worldPosition.relative(direction, 1));
            if (extractEntity == null) return;
            updateAutoExtractCache(direction, extractEntity);
        });

        for (var target : autoExtractCache.entrySet()) {
            var outputEmpty = new AtomicBoolean(false);
            target.getValue().ifPresent(targetInv -> {
                var stack = inventory.getStackInOutput();
                var remainder = ItemHandlerHelper.insertItemStacked(targetInv, stack, false);
                if (remainder.isEmpty()) {
                    inventory.setStackInOutput(ItemStack.EMPTY);
                    outputEmpty.set(true);
                } else if (remainder.getCount() < stack.getCount()) {
                    inventory.getStackInOutput().setCount(remainder.getCount());
                }
            });

            if (outputEmpty.get()) return;
        }
    }

    private void updateAutoExtractCache(Direction direction, BlockEntity provider) {
        autoExtractCache.computeIfAbsent(direction, d -> {
            var target = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
            autoExtractCache.put(d, target);
            target.addListener(self -> autoExtractCache.put(d, null));
            return target;
        });
    }

    @Nullable
    private ProcessorRecipe detectRecipeFromInventory() {
        assert level != null && !level.isClientSide;
        return GameUtil.getRecipeManager(level)
            .getRecipeFor(getProcessorType(), inventory.toContainer(), level)
            .orElse(null);
    }

    @Nullable
    private ProcessorRecipe getRecipe() {
        assert level != null && !level.isClientSide;
        if (recipeCache != null && recipeCache.matches(inventory.toContainer(), level)) {
            return recipeCache;
        }
        var recipe = detectRecipeFromInventory();
        if (recipe == null) {
            stopWork();
            return null;
        }
        recipeCache = recipe;
        return recipe;
    }

    public int getEnergyCost() {
        return energyCost;
    }

    void setEnergyCost(int energyCost) {
        this.energyCost = energyCost;
    }

    public int getRecipeEnergy() {
        return recipeEnergy;
    }

    void setRecipeEnergy(int recipeEnergy) {
        this.recipeEnergy = recipeEnergy;
    }

    public ProcessorType getProcessorType() {
        return ((ProcessorBlock) getBlockState().getBlock()).getProcessorType();
    }

    public int getProgress() {
        return progress;
    }

    void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    void setProcessTime(int processTime) {
        this.processTime = processTime;
    }

    public int getRecipeTime() {
        return recipeTime;
    }

    void setRecipeTime(int recipeTime) {
        this.recipeTime = recipeTime;
    }

    public boolean isAutoExtracting() {
        return autoExtract;
    }

    public void setAutoExtract(boolean autoExtract) {
        this.autoExtract = autoExtract;
    }
}
