package com.almostreliable.lazierae2.content.processor;

import com.almostreliable.lazierae2.network.sync.IDataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import static com.almostreliable.lazierae2.core.Constants.Nbt.*;

public class EnergyHandler implements IEnergyStorage, INBTSerializable<CompoundTag>, IDataHandler {

    private final ProcessorEntity entity;
    protected int energy;
    private int capacity;
    private int maxReceive;
    private int maxExtract;
    private boolean changed;

    EnergyHandler(ProcessorEntity entity) {
        this.entity = entity;
        var baseEnergyBuffer = entity.getProcessorType().getBaseEnergyBuffer();
        capacity = baseEnergyBuffer;
        maxReceive = baseEnergyBuffer;
        maxExtract = baseEnergyBuffer;
        energy = 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        var energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            onChange();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;
        var energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            onChange();
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(energy);
        buffer.writeInt(capacity);
        changed = false;
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        energy = buffer.readInt();
        capacity = buffer.readInt();
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt(CAPACITY_ID, capacity);
        tag.putInt(ENERGY_ID, energy);
        tag.putInt(MAX_RECEIVE, maxReceive);
        tag.putInt(MAX_EXTRACT, maxExtract);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        capacity = tag.getInt(CAPACITY_ID);
        energy = tag.getInt(ENERGY_ID);
        maxReceive = tag.getInt(MAX_RECEIVE);
        maxExtract = tag.getInt(MAX_EXTRACT);
        onChange();
    }

    private void onChange() {
        entity.setChanged();
        changed = true;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
        onChange();
    }

    void setCapacity(int capacity) {
        this.capacity = capacity;
        energy = Math.min(energy, capacity);
        maxReceive = capacity;
        maxExtract = capacity;
        onChange();
    }
}
