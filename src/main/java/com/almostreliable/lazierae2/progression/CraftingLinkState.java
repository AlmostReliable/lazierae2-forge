package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public final class CraftingLinkState extends ProgressionState {

    private final ICraftingLink link;

    public CraftingLinkState(ICraftingLink link) {
        super(PROGRESSION_TYPE.LINK);
        this.link = link;
    }

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (link.isDone()) {
            return new ExportState();
        }

        if (link.isCanceled()) {
            return new IdleState();
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
