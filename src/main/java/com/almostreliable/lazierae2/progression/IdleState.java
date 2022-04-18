package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

public class IdleState extends MaintainerProgressionState {
    public IdleState(MaintainerEntity owner, int slot) {
        super(owner, slot);
    }

    @Nullable
    @Override
    public ProgressionState handle() {
        if (owner.getCraftResults().getAmount(slot) > 0) {
            return new ExportSlotState(owner, slot);
        }

        if (owner.getCraftRequests().get(slot).state()) {
            return new RequestCraftState(owner, slot);
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE; //TODO
    }
}
