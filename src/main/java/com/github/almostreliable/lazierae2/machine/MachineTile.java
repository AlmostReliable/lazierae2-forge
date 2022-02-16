package com.github.almostreliable.lazierae2.machine;

import com.github.almostreliable.lazierae2.component.EnergyHandler;
import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.component.SideConfiguration;
import com.github.almostreliable.lazierae2.core.Setup.Tiles;
import com.github.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.github.almostreliable.lazierae2.recipe.MachineType;
import com.github.almostreliable.lazierae2.recipe.MachineRecipe;
import com.github.almostreliable.lazierae2.util.GameUtil;
import com.github.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.block.BlockState;
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
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class MachineTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

    private final InventoryHandler inventory;
    private final LazyOptional<InventoryHandler> inventoryCap;
    private final EnergyHandler energy;
    private final LazyOptional<EnergyHandler> energyCap;
    private final SideConfiguration sideConfig;
    private final Map<Direction, LazyOptional<IItemHandler>> outputsCache = new EnumMap<>(Direction.class);
    private boolean autoExtract;
    private int progress;
    private int processTime;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    MachineTile(int inputSlots) {
        super(Tiles.MACHINE.get());
        inventory = new InventoryHandler(this, inputSlots);
        inventoryCap = LazyOptional.of(() -> inventory);
        energy = new EnergyHandler(this, 100_000);
        energyCap = LazyOptional.of(() -> energy);
        sideConfig = new SideConfiguration();
    }

    /*
     * Constructor called from the registry.
     * It will call the super constructor and the input slot amount will then be
     * handled by the load-method since we have no way of accessing block
     * information from here.
     */
    public MachineTile() {
        this(0);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains(INVENTORY_ID)) inventory.deserializeNBT(nbt.getCompound(INVENTORY_ID));
        if (nbt.contains(ENERGY_ID)) energy.deserializeNBT(nbt.getCompound(ENERGY_ID));
        if (nbt.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
        if (nbt.contains(AUTO_EXTRACT_ID)) autoExtract = nbt.getBoolean(AUTO_EXTRACT_ID);
        if (nbt.contains(PROGRESS_ID)) progress = nbt.getInt(PROGRESS_ID);
        if (nbt.contains(PROCESS_TIME_ID)) processTime = nbt.getInt(PROCESS_TIME_ID);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put(INVENTORY_ID, inventory.serializeNBT());
        nbt.put(ENERGY_ID, energy.serializeNBT());
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        nbt.putBoolean(AUTO_EXTRACT_ID, autoExtract);
        nbt.putInt(PROGRESS_ID, progress);
        nbt.putInt(PROCESS_TIME_ID, processTime);
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

        // TODO: make auto extract interval configurable
        if (autoExtract && level.getGameTime() % 20 == 0) autoExtract();

        // TODO: cache last recipe and check it first
        MachineRecipe recipe = getRecipe();
        if (recipe == null) {
            stopWork();
            return;
        }

        int energyCost = calculateEnergyCost(recipe);
        if (canWork(recipe, energyCost)) {
            doWork(recipe, energyCost);
        } else {
            setActivityState(false);
        }
    }

    @Override
    protected void invalidateCaps() {
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

    private void doWork(MachineRecipe recipe, int energyCost) {
        processTime = calculateProcessTime(recipe);
        energy.setEnergy(energy.getEnergyStored() - energyCost);

        if (progress < processTime) {
            setActivityState(true);
            progress++;
        } else {
            finishWork(recipe);
        }
    }

    private void finishWork(IRecipe<? super IInventory> recipe) {
        if (inventory.getStackInOutput().isEmpty()) {
            inventory.setStackInOutput(recipe.assemble(new RecipeWrapper(inventory)));
        } else {
            inventory.getStackInOutput().grow(recipe.getResultItem().getCount());
        }

        inventory.shrinkInputSlots();
        stopWork();
    }

    private void updateState(BlockState state) {
        if (level == null) return;
        BlockState oldState = level.getBlockState(worldPosition);
        if (!oldState.equals(state)) {
            level.setBlock(worldPosition, state, BlockFlags.NOTIFY_NEIGHBORS | BlockFlags.BLOCK_UPDATE);
        }
    }

    private void stopWork() {
        setActivityState(false);
        progress = 0;
    }

    private int calculateEnergyCost(MachineRecipe recipe) {
        int baseCost = recipe.getEnergyCost();
        double upgradeMultiplier = getUpgradeEnergyMultiplier();
        return (int) (baseCost * upgradeMultiplier);
    }

    private int calculateProcessTime(MachineRecipe recipe) {
        int baseTime = recipe.getProcessTime();
        double upgradeMultiplier = getUpgradeProcessTimeMultiplier();
        return (int) (baseTime * upgradeMultiplier);
    }

    private boolean canWork(IRecipe<IInventory> recipe, int energyCost) {
        if (energyCost > energy.getEnergyStored()) return false;

        ItemStack current = inventory.getStackInOutput();
        if (current.isEmpty()) return true;

        ItemStack finished = recipe.getResultItem();
        int mergeCount = current.getCount() + finished.getCount();
        return current.sameItem(finished) && mergeCount <= finished.getMaxStackSize() &&
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
                // TODO make auto extract amount per operation configurable
                ItemStack stack = inventory.getStackInOutput();
                ItemStack remainder = ItemHandlerHelper.insertItem(targetInv, stack, false);

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

    private double getUpgradeEnergyMultiplier() {
        int upgradeCount = inventory.getUpgradeCount();
        if (upgradeCount == 0) return 1.0;
        return getMachineType().getUpgradeEnergyMultiplier() * upgradeCount;
    }

    private double getUpgradeProcessTimeMultiplier() {
        int upgradeCount = inventory.getUpgradeCount();
        if (upgradeCount == 0) return 1.0;
        return getMachineType().getUpgradeProcessTimeMultiplier() * upgradeCount;
    }

    @Nullable
    private MachineRecipe getRecipe() {
        assert level != null;
        return GameUtil
            .getRecipeManager(level)
            .getRecipeFor(getMachineType(), new RecipeWrapper(inventory), level)
            .orElse(null);
    }
    
    public SideConfiguration getSideConfig() {
        return sideConfig;
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

    public boolean isAutoExtract() {
        return autoExtract;
    }

    public void setAutoExtract(boolean autoExtract) {
        this.autoExtract = autoExtract;
    }

    private void setActivityState(boolean active) {
        if (level == null) return;
        BlockState state = level.getBlockState(worldPosition);
        if (active && state.getValue(MachineBlock.ACTIVE).equals(false)) {
            updateState(state.setValue(MachineBlock.ACTIVE, true));
        } else if (!active && state.getValue(MachineBlock.ACTIVE).equals(true)) {
            updateState(state.setValue(MachineBlock.ACTIVE, false));
        }
    }
}
