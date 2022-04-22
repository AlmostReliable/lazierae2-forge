package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public record ClientState(PROGRESSION_TYPE type) implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        throw new UnsupportedOperationException();
    }
}
