package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public record ClientState(PROGRESSION_TYPE type) implements IProgressionState {

    @Override
    public IProgressionState handle(RequesterEntity owner, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        throw new UnsupportedOperationException();
    }
}
