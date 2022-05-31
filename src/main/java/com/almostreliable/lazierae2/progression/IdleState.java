package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public class IdleState implements IProgressionState {

    IdleState() {}

    @Override
    public IProgressionState handle(RequesterEntity owner, int slot) {
        if (owner.getStorageManager().get(slot).getBufferAmount() > 0) {
            return IProgressionState.EXPORT;
        }

        var request = owner.craftRequests.get(slot);
        if (request.isRequesting() && request.getCount() > owner.getStorageManager().get(slot).getKnownAmount()) {
            return IProgressionState.REQUEST;
        }

        return this;
    }

    @Override
    public PROGRESSION_TYPE type() {
        return PROGRESSION_TYPE.IDLE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
