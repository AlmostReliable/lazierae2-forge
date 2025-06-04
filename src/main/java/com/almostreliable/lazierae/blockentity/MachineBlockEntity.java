package com.almostreliable.lazierae.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.almostreliable.lazierae.inventory.EnergyStorage;
import com.almostreliable.lazierae.inventory.EnergyStorageHost;
import com.almostreliable.lazierae.inventory.InventoryHost;
import com.almostreliable.lazierae.inventory.MachineInventory;
import com.almostreliable.lazierae.inventory.UpgradeInventory;
import com.almostreliable.lib.component.ComponentHolder;
import com.almostreliable.lib.component.impl.SimpleEnergyContainer;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.grid.AENetworkBlockEntity;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

public abstract class MachineBlockEntity extends AENetworkBlockEntity
    implements ComponentHolder, InventoryHost, EnergyStorageHost, IAEPowerStorage, IGridTickable, MenuProvider {

    protected final MachineInventory inventory;
    protected final UpgradeInventory upgrades;
    protected final SimpleEnergyContainer energy;

    @Nullable
    protected Consumer<Object> invalidateListener;
    protected MachineState currentState = MachineState.OK;

    protected MachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.inventory = new MachineInventory(this, 1);
        this.upgrades = new UpgradeInventory(this);
        this.energy = new EnergyStorage(this, 100_000);

        getMainNode().setIdlePowerUsage(0)
            .setExposedOnSides(EnumSet.noneOf(Direction.class))
            .addService(IAEPowerStorage.class, this)
            .addService(IGridTickable.class, this);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.put("inventory", inventory.serialize());
        data.put("upgrades", upgrades.serialize());
        data.put("energy", energy.serialize());
        data.putString("state", currentState.name());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        if (data.contains("inventory")) inventory.deserialize(data.getCompound("inventory"));
        if (data.contains("upgrades")) upgrades.deserialize(data.getCompound("upgrades"));
        if (data.contains("energy")) energy.deserialize(data.getCompound("energy"));
        if (data.contains("state")) currentState = MachineState.valueOf(data.getString("state"));
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void addInvalidateListener(Consumer<Object> listener) {
        this.invalidateListener = listener;
    }

    @Nullable
    @Override
    public Consumer<Object> getInvalidateListener() {
        return invalidateListener;
    }

    @Override
    public double injectAEPower(double amount, Actionable mode) {
        long toInsert = (long) Math.min(PowerUnits.AE.convertTo(PowerUnits.RF, amount), Long.MAX_VALUE);
        long inserted = energy.insert(toInsert, mode == Actionable.SIMULATE);
        return amount - inserted;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        return 0;
    }

    @Override
    public double getAEMaxPower() {
        return PowerUnits.RF.convertTo(PowerUnits.AE, energy.getCapacity());
    }

    @Override
    public double getAECurrentPower() {
        return PowerUnits.RF.convertTo(PowerUnits.AE, energy.getAmount());
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.WRITE;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, true, false);
    }

    @Override
    public void onReady() {
        updateExposedNodeSides();
        super.onReady();
        wakeUp();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateContainer(inventory);
        invalidateContainer(energy);
    }

    @MustBeInvokedByOverriders
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        inventory.addDrops(drops);
        upgrades.addDrops(drops);
    }

    protected void wakeUp() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    protected abstract void updateExposedNodeSides();

    public MachineInventory getInventory() {
        return inventory;
    }

    public UpgradeInventory getUpgrades() {
        return upgrades;
    }

    public MachineState getCurrentState() {
        return currentState;
    }
}
