package com.almostreliable.lazierae.inventory;

public interface EnergyStorageHost {

    void saveChanges();

    /**
     * Called when the energy stored in the energy container changes.
     */
    void onEnergyChange();
}
