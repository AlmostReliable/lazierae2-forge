package com.almostreliable.lazierae2.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.StorageHelper;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public class ExportState implements IProgressionState {

    ExportState() {}

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        var storage = owner.getStorageManager().get(0);
        if (storage.getItemType() == null) {
            return IProgressionState.IDLE;
        }

        var inserted = StorageHelper.poweredInsert(
            owner.getMainNodeGrid().getEnergyService(),
            owner.getMainNodeGrid().getStorageService().getInventory(),
            storage.getItemType(),
            storage.getBufferAmount(),
            owner.getActionSource(),
            Actionable.MODULATE
        );

        if (storage.compute(inserted)) {
            return this;
        }
        if (inserted > 0) {
            return IProgressionState.REQUEST;
        }
        return IProgressionState.IDLE;
    }

    @Override
    public PROGRESSION_TYPE type() {
        return PROGRESSION_TYPE.EXPORT;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
