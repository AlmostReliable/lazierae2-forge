package com.almostreliable.lazierae2.machine;

import com.almostreliable.lazierae2.component.EnergyHandler;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.component.SideConfiguration;
import com.almostreliable.lazierae2.core.Setup.Tiles;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
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

public class MachineTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    public final SideConfiguration sideConfig;
    private final InventoryHandler inventory;
    private final LazyOptional<InventoryHandler> inventoryCap;
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
    MachineTile(int inputSlots, int energyBuffer) {
        super(Tiles.MACHINE.get());
        inventory = new InventoryHandler(this, inputSlots);
        inventoryCap = LazyOptional.of(() -> inventory);
        energy = new EnergyHandler(this, energyBuffer);
        energyCap = LazyOptional.of(() -> energy);
        sideConfig = new SideConfiguration(this);
    }

    /*
     * Constructor called from the registry.
     * It will call the main constructor with placeholder values.
     * The actual values will then be handled by the load-method
     * since we have no way of accessing block information from here.
     */
    public MachineTile() {
        this(0, 0);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains(INVENTORY_ID)) inventory.deserializeNBT(nbt.getCompound(INVENTORY_ID));
        if (nbt.contains(ENERGY_ID)) energy.deserializeNBT(nbt.getCompound(ENERGY_ID));
        if (nbt.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(state, nbt.getCompound(SIDE_CONFIG_ID));
        if (nbt.contains(AUTO_EXTRACT_ID)) autoExtract = nbt.getBoolean(AUTO_EXTRACT_ID);
        if (nbt.contains(PROGRESS_ID)) progress = nbt.getInt(PROGRESS_ID);
        if (nbt.contains(PROCESS_TIME_ID)) processTime = nbt.getInt(PROCESS_TIME_ID);
        if (nbt.contains(RECIPE_TIME_ID)) recipeTime = nbt.getInt(RECIPE_TIME_ID);
        if (nbt.contains(ENERGY_COST_ID)) energyCost = nbt.getInt(ENERGY_COST_ID);
        if (nbt.contains(RECIPE_ENERGY_ID)) recipeEnergy = nbt.getInt(RECIPE_ENERGY_ID);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put(INVENTORY_ID, inventory.serializeNBT());
        nbt.put(ENERGY_ID, energy.serializeNBT());
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        nbt.putBoolean(AUTO_EXTRACT_ID, autoExtract);
        nbt.putInt(PROGRESS_ID, progress);
        nbt.putInt(PROCESS_TIME_ID, processTime);
        nbt.putInt(RECIPE_TIME_ID, recipeTime);
        nbt.putInt(ENERGY_COST_ID, energyCost);
        nbt.putInt(RECIPE_ENERGY_ID, recipeEnergy);
        return super.save(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(super.getUpdateTag());
    }

    @Nullable
    @Override
    public Container createMenu(
        int menuID, PlayerInventory playerInventory, PlayerEntity player
    ) {
        return new MachineContainer(menuID, this, playerInventory);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        load(state, nbt);
    }

    @Override
    public void tick() {
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

    public void recalculateEnergyCapacity() {
        if (level == null || level.isClientSide) return;
        int baseBuffer = getMachineType().getBaseEnergyBuffer();
        int upgradeBuffer = getMachineType().getEnergyBufferAdd();
        int newCapacity = baseBuffer + upgradeBuffer * inventory.getUpgradeCount();
        if (newCapacity != energy.getMaxEnergyStored()) energy.setCapacity(newCapacity);
    }

    @Override
    protected void invalidateCaps() {
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
                return inventory.getInventoryCap(sideConfig.get(direction)).cast();
            }
            if (cap.equals(CapabilityEnergy.ENERGY)) {
                return energyCap.cast();
            }
        }
        return super.getCapability(cap, direction);
    }

    void playerDestroy() {
        assert level != null;
        inventory.dropContents();
        CompoundNBT nbt = new CompoundNBT();
        if (inventory.getUpgradeCount() > 0) nbt.put(UPGRADES_ID, inventory.serializeUpgrades());
        if (energy.getEnergyStored() > 0) nbt.put(ENERGY_ID, energy.serializeNBT());
        if (sideConfig.hasChanged()) nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        if (autoExtract) nbt.putBoolean(AUTO_EXTRACT_ID, true);
        ItemStack stack = new ItemStack(getMachineType().getItemProvider());
        if (!nbt.isEmpty()) stack.setTag(nbt);
        level.addFreshEntity(new ItemEntity(level,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            stack
        ));
    }

    void playerPlace(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) return;
        if (nbt.contains(UPGRADES_ID)) inventory.deserializeUpgrades(nbt.getCompound(UPGRADES_ID));
        if (nbt.contains(ENERGY_ID)) energy.deserializeNBT(nbt.getCompound(ENERGY_ID));
        if (nbt.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
        if (nbt.contains(AUTO_EXTRACT_ID)) autoExtract = nbt.getBoolean(AUTO_EXTRACT_ID);
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

    private void finishWork(IRecipe<? super IInventory> recipe) {
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
        int baseCost = recipe.getEnergyCost();
        double multiplier = calculateMultiplier(getMachineType().getEnergyCostMultiplier());
        return (int) (baseCost * multiplier);
    }

    private int calculateProcessTime(MachineRecipe recipe) {
        int baseTime = recipe.getProcessTime();
        double multiplier = calculateMultiplier(getMachineType().getProcessTimeMultiplier());
        return (int) Math.max(1.0, baseTime * multiplier);
    }

    private double calculateMultiplier(double upgradeMultiplier) {
        int upgradeCount = inventory.getUpgradeCount();
        return Math.pow(upgradeMultiplier, upgradeCount);
    }

    private boolean canWork(IRecipe<IInventory> recipe, int energyCost) {
        if (energyCost / processTime > energy.getEnergyStored()) return false;

        ItemStack output = inventory.getStackInOutput();
        if (output.isEmpty()) return true;

        ItemStack finished = recipe.getResultItem();
        int mergeCount = output.getCount() + finished.getCount();
        return output.sameItem(finished) && mergeCount <= finished.getMaxStackSize() &&
            mergeCount <= inventory.getSlotLimit(InventoryHandler.OUTPUT_SLOT);
    }

    private void autoExtract() {
        assert level != null;

        if (inventory.getStackInOutput().isEmpty()) return;

        EnumMap<Direction, TileEntity> possibleOutputs = new EnumMap<>(Direction.class);
        sideConfig.forEachOutput(direction -> possibleOutputs.put(direction,
            level.getBlockEntity(worldPosition.relative(direction, 1))
        ));

        for (Entry<Direction, TileEntity> entry : possibleOutputs.entrySet()) {
            LazyOptional<IItemHandler> target = getOrUpdateOutputCache(entry);
            if (target == null) continue;

            AtomicBoolean outputEmpty = new AtomicBoolean(false);
            target.ifPresent(targetInv -> {
                ItemStack stack = inventory.getStackInOutput();
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(targetInv, stack, false);

                if (remainder.getCount() != stack.getCount() || !remainder.sameItem(stack)) {
                    inventory.setStackInOutput(remainder);
                }
                if (remainder.isEmpty()) outputEmpty.set(true);
            });

            if (outputEmpty.get()) return;
        }
    }

    @Nullable
    private LazyOptional<IItemHandler> getOrUpdateOutputCache(Entry<Direction, ? extends TileEntity> entry) {
        LazyOptional<IItemHandler> target = outputsCache.get(entry.getKey());

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

    private void changeActivityState(boolean state) {
        if (level == null || level.isClientSide) return;
        BlockState oldState = level.getBlockState(worldPosition);
        if (!oldState.getValue(MachineBlock.ACTIVE).equals(state)) {
            level.setBlockAndUpdate(worldPosition, oldState.setValue(MachineBlock.ACTIVE, state));
        }
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
    public ITextComponent getDisplayName() {
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
