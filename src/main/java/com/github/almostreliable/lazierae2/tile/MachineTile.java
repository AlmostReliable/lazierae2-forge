package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.component.EnergyHandler;
import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.component.SideConfiguration;
import com.github.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public abstract class MachineTile extends TileEntity implements ITickableTileEntity {
    private final int inputSlots;
    private final InventoryHandler inventory;
    private final LazyOptional<InventoryHandler> inventoryCap;
    private final EnergyHandler energy;
    private final LazyOptional<EnergyHandler> energyCap;
    private final SideConfiguration sideConfig;
    private int progress;
    private int processTime;
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    protected MachineTile(TileEntityType<?> type, int inputSlots) {
        super(type);
        this.inputSlots = inputSlots;
        inventory = new InventoryHandler(this, inputSlots);
        inventoryCap = LazyOptional.of(() -> inventory);
        energy = new EnergyHandler(this, 100_000);
        energyCap = LazyOptional.of(() -> energy);
        sideConfig = new SideConfiguration();
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains(INVENTORY_ID)) inventory.deserializeNBT(nbt.getCompound(INVENTORY_ID));
        if (nbt.contains(ENERGY_ID)) energy.deserializeNBT(nbt.getCompound(ENERGY_ID));
        if (nbt.contains(SIDE_CONFIG_ID)) sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put(INVENTORY_ID, inventory.serializeNBT());
        nbt.put(ENERGY_ID, energy.serializeNBT());
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        return super.save(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.put(INVENTORY_ID, inventory.serializeNBT());
        nbt.put(ENERGY_ID, energy.serializeNBT());
        nbt.put(SIDE_CONFIG_ID, sideConfig.serializeNBT());
        return nbt;
        // TODO: can also be handled by save() if not speficic values need syncing
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        inventory.deserializeNBT(nbt.getCompound(INVENTORY_ID));
        energy.deserializeNBT(nbt.getCompound(ENERGY_ID));
        sideConfig.deserializeNBT(nbt.getCompound(SIDE_CONFIG_ID));
        // TODO: can also be handled by load() if not speficic values need syncing
    }

    @Override
    public void tick() {
        // TODO
        // testing to sync progress
        progress++;
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

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getInputSlots() {
        return inputSlots;
    }
}
