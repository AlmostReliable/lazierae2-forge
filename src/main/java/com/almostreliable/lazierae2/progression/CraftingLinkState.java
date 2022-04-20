package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public record CraftingLinkState(ICraftingLink link) implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        if (link.isDone()) {
            return IProgressionState.EXPORT_SLOT_STATE;
        }

        if (link.isCanceled()) {
            return IProgressionState.IDLE_STATE;
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SAME;
    }
}
