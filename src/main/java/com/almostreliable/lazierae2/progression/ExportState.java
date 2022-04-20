package com.almostreliable.lazierae2.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.StorageHelper;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public class ExportState implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        var storage = owner.getStorageManager().get(0);
        if (storage.getItemType() == null) {
            return IProgressionState.IDLE_STATE;
        }

        var inserted = StorageHelper.poweredInsert(
            owner.getMainNodeGrid().getEnergyService(),
            owner.getMainNodeGrid().getStorageService().getInventory(),
            storage.getItemType(),
            storage.getBufferAmount(),
            owner.getActionSource(),
            Actionable.MODULATE
        );

        if (inserted > 0) {
            if (storage.compute(inserted)) {
                return this;
            }
            return IProgressionState.REQUEST_CRAFT_STATE;
        }

        return IProgressionState.IDLE_STATE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
