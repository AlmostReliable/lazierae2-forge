package com.almostreliable.lazierae2.content.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler.MachineInventory;
import com.almostreliable.lazierae2.component.SideConfiguration;
import com.almostreliable.lazierae2.content.GenericEntity;
import com.almostreliable.lazierae2.core.Setup.Entities;
import com.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.almostreliable.lazierae2.core.Constants.*;

public class MachineEntity extends GenericEntity {

    public final SideConfiguration sideConfig;
    private final MachineInventory inventory;
    private final LazyOptional<MachineInventory> inventoryCap;
    private final EnergyHandler energy;
    private final LazyOptional<EnergyHandler> energyCap;
    private final Map<Direction, LazyOptional<IItemHandler>> outputsCache = new EnumMap<>(Direction.class);
    private boolean autoExtract;
    private int progress;
    private int processTime;
    private int recipeTime;
    private int energyCost;
    private int recipeEnergy;
    private MachineRecipe lastRecipe;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public MachineEntity(BlockPos pos, BlockState state) {
        super(Entities.MACHINE.get(), pos, state);
        inventory = new MachineInventory(this);
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
        return new MachineMenu(menuID, this, inventory);
    }

    public void recalculateEnergyCapacity() {
        if (level == null || level.isClientSide) return;
        var baseBuffer = getMachineType().getBaseEnergyBuffer();
        var upgradeBuffer = getMachineType().getEnergyBufferAdd();
        var newCapacity = baseBuffer + upgradeBuffer * inventory.getUpgradeCount();
        if (newCapacity != energy.getMaxEnergyStored()) energy.setCapacity(newCapacity);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
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
                if (direction == null || sideConfig.get(direction) != IO_SETTING.OFF) return inventoryCap.cast();
            } else if (cap.equals(CapabilityEnergy.ENERGY)) {
                return energyCap.cast();
            }
        }
        return super.getCapability(cap, direction);
    }

    void tick() {
        if (level == null || level.isClientSide) return;
        if (autoExtract && level.getGameTime() % 10 == 0) autoExtract();
        energy.validateEnergy();

        MachineRecipe recipe;
        if (lastRecipe != null && lastRecipe.matches(inventory.toVanilla(), level)) {
            recipe = lastRecipe;
        } else {
            recipe = getRecipe();
            if (recipe == null) {
                stopWork();
                return;
            }
            lastRecipe = recipe;
        }

        energyCost = calculateEnergyCost(recipe);
        recipeEnergy = recipe.getEnergyCost();
        recipeTime = recipe.getProcessTime();
        processTime = calculateProcessTime(recipe);
        if (canWork(recipe, energyCost)) {
            doWork(recipe, energyCost);
        } else {
            changeActivityState(false);
        }
    }

    void playerDestroy() {
        assert level != null;
        inventory.dropContents();
        var tag = new CompoundTag();
        if (inventory.getUpgradeCount() > 0) tag.put(UPGRADES_ID, inventory.serializeUpgrades());
        if (energy.getEnergyStored() > 0) tag.put(ENERGY_ID, energy.serializeNBT());
        if (sideConfig.hasChanged()) tag.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        if (autoExtract) tag.putBoolean(AUTO_EXTRACT_ID, true);
        var stack = new ItemStack(getMachineType().getItemProvider());
        if (!tag.isEmpty()) stack.setTag(tag);
        level.addFreshEntity(new ItemEntity(level,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            stack
        ));
    }

    void playerPlace(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return;
        if (tag.contains(UPGRADES_ID)) inventory.deserializeUpgrades(tag.getCompound(UPGRADES_ID));
        if (tag.contains(ENERGY_ID)) energy.deserializeNBT(tag.getCompound(ENERGY_ID));
        if (tag.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(tag.getCompound(SIDE_CONFIG_ID));
        if (tag.contains(AUTO_EXTRACT_ID)) autoExtract = tag.getBoolean(AUTO_EXTRACT_ID);
    }

    private void doWork(MachineRecipe recipe, int energyCost) {
        if (progress < processTime) {
            changeActivityState(true);
            energy.setEnergy(energy.getEnergyStored() - (energyCost / processTime));
            progress++;
            setChanged();
        } else {
            finishWork(recipe);
        }
    }

    private void finishWork(Recipe<? super Container> recipe) {
        if (inventory.getStackInOutput().isEmpty()) {
            inventory.setStackInOutput(recipe.assemble(inventory.toVanilla()));
        } else {
            inventory.getStackInOutput().grow(recipe.getResultItem().getCount());
        }

        inventory.shrinkInputSlots();
        progress = 0;
        setChanged();
    }

    private void stopWork() {
        changeActivityState(false);
        progress = 0;
        lastRecipe = null;
    }

    private int calculateEnergyCost(MachineRecipe recipe) {
        var baseCost = recipe.getEnergyCost();
        var multiplier = calculateMultiplier(getMachineType().getEnergyCostMultiplier());
        return (int) (baseCost * multiplier);
    }

    private int calculateProcessTime(MachineRecipe recipe) {
        var baseTime = recipe.getProcessTime();
        var multiplier = calculateMultiplier(getMachineType().getProcessTimeMultiplier());
        return (int) (baseTime * multiplier);
    }

    private double calculateMultiplier(double upgradeMultiplier) {
        var upgradeCount = inventory.getUpgradeCount();
        return Math.pow(upgradeMultiplier, upgradeCount);
    }

    private boolean canWork(Recipe<Container> recipe, int energyCost) {
        if (energyCost / processTime > energy.getEnergyStored()) return false;

        var output = inventory.getStackInOutput();
        if (output.isEmpty()) return true;

        var finished = recipe.getResultItem();
        var mergeCount = output.getCount() + finished.getCount();
        return output.sameItem(finished) && mergeCount <= finished.getMaxStackSize() &&
            mergeCount <= inventory.getSlotLimit(MachineInventory.OUTPUT_SLOT);
    }

    private void autoExtract() {
        assert level != null;

        if (inventory.getStackInOutput().isEmpty()) return;

        var possibleOutputs = new EnumMap<Direction, BlockEntity>(Direction.class);
        sideConfig.forEachOutput(direction -> possibleOutputs.put(direction,
            level.getBlockEntity(worldPosition.relative(direction, 1))
        ));

        for (var entry : possibleOutputs.entrySet()) {
            var target = getOrUpdateOutputCache(entry);
            if (target == null) continue;

            var outputEmpty = new AtomicBoolean(false);
            target.ifPresent(targetInv -> {
                var stack = inventory.getStackInOutput();
                var remainder = ItemHandlerHelper.insertItemStacked(targetInv, stack, false);

                if (remainder.getCount() != stack.getCount() || !remainder.sameItem(stack)) {
                    inventory.setStackInOutput(remainder);
                }
                if (remainder.isEmpty()) outputEmpty.set(true);
            });

            if (outputEmpty.get()) return;
        }
    }

    @Nullable
    private LazyOptional<IItemHandler> getOrUpdateOutputCache(Entry<Direction, ? extends BlockEntity> entry) {
        var target = outputsCache.get(entry.getKey());

        if (target == null) {
            ICapabilityProvider provider = entry.getValue();
            if (provider == null) return null;
            target = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                entry.getKey().getOpposite()
            );
            outputsCache.put(entry.getKey(), target);
            target.addListener(self -> outputsCache.put(entry.getKey(), null));
        }
        return target;
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

    @Nullable
    private MachineRecipe getRecipe() {
        assert level != null;
        return GameUtil
            .getRecipeManager(level)
            .getRecipeFor(getMachineType(), inventory.toVanilla(), level)
            .orElse(null);
    }

    public MachineType getMachineType() {
        return ((MachineBlock) getBlockState().getBlock()).getMachineType();
    }

    @Override
    public Component getDisplayName() {
        return TextUtil.translate(TRANSLATE_TYPE.BLOCK, getMachineType().getId());
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
