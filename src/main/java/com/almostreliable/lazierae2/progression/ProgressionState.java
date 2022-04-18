package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;

import javax.annotation.Nullable;

public interface ProgressionState {
    /**
     * @return the next state, self, or null if the progression is complete.
     */
    @Nullable
    ProgressionState handle();

    TickRateModulation getTickRateModulation();

    void interrupt();
}
