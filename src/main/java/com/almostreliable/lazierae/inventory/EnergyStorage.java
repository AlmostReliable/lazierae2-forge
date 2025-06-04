package com.almostreliable.lazierae.inventory;

import com.almostreliable.lib.component.impl.SimpleEnergyContainer;

public class EnergyStorage extends SimpleEnergyContainer {

    private final EnergyStorageHost host;

    public EnergyStorage(EnergyStorageHost host, long capacity) {
        super(capacity);
        this.host = host;
    }

    @Override
    protected void onChange() {
        host.onEnergyChange();
        host.saveChanges();
    }
}
