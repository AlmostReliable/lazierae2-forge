package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public record CraftingLinkState(ICraftingLink link) implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        if (link.isDone()) {
            return IProgressionState.EXPORT;
        }

        if (link.isCanceled()) {
            return IProgressionState.IDLE;
        }

        return this;
    }

    @Override
    public PROGRESSION_TYPE type() {
        return PROGRESSION_TYPE.LINK;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SAME;
    }
}
