package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public class IdleState extends ProgressionState {

    public IdleState() {
        super(PROGRESSION_TYPE.IDLE);
    }

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (owner.getStorageManager().get(slot).getBufferAmount() > 0) {
            return new ExportState();
        }

        var request = owner.getCraftRequests().get(slot);
        if (request.isRequesting() && request.count() > owner.getStorageManager().get(slot).getKnownAmount()) {
            return new RequestState();
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
