package com.github.almostreliable.lazierae2.component;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

import static com.github.almostreliable.lazierae2.core.Constants.CAPACITY_ID;
import static com.github.almostreliable.lazierae2.core.Constants.ENERGY_ID;

public class EnergyHandler extends EnergyStorage implements INBTSerializable<CompoundNBT> {

    private final TileEntity tile;

    public EnergyHandler(TileEntity tile, int capacity) {
        super(capacity);
        this.tile = tile;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
        tile.setChanged();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(CAPACITY_ID, capacity);
        nbt.putInt(ENERGY_ID, energy);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        capacity = nbt.getInt(CAPACITY_ID);
        energy = nbt.getInt(ENERGY_ID);
    }
}
