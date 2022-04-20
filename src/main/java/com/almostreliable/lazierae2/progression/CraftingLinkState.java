package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

public class CraftingLinkState implements ProgressionState {
    private final ICraftingLink link;

    public CraftingLinkState(ICraftingLink link) {
        this.link = link;
    }

    @Nullable
    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (link.isDone()) {
            return ProgressionState.EXPORT_SLOT_STATE;
        }

        if (link.isCanceled()) {
            return ProgressionState.IDLE_STATE;
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SAME;
    }

    public ICraftingLink getLink() {
        return link;
    }
}
