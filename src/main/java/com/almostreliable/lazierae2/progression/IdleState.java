package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public class IdleState implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        if (owner.getStorageManager().get(slot).getBufferAmount() > 0) {
            return IProgressionState.EXPORT_SLOT_STATE;
        }

        var request = owner.getCraftRequests().get(slot);
        if (request.isRequesting() && request.count() > owner.getStorageManager().get(slot).getKnownAmount()) {
            return IProgressionState.REQUEST_CRAFT_STATE;
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
