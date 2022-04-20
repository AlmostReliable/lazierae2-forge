package com.almostreliable.lazierae2.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.StorageHelper;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.StorageManager;

public class ExportSlotState implements ProgressionState {

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        StorageManager.Storage storage = owner.getStorageManager().get(0);
        if (storage.getKey() == null) {
            return ProgressionState.IDLE_STATE;
        }

        long inserted = StorageHelper.poweredInsert(
            owner.getMainNodeGrid().getEnergyService(),
            owner.getMainNodeGrid().getStorageService().getInventory(),
            storage.getKey(),
            storage.getBuffer(),
            owner.getActionSource(),
            Actionable.MODULATE
        );

        if (inserted > 0) {
            if (storage.compute(inserted)) {
                return this;
            } else {
                return ProgressionState.REQUEST_CRAFT_STATE;
            }
        }

        return ProgressionState.IDLE_STATE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
