package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

public class CraftingLinkState extends MaintainerProgressionState {
    private final ICraftingLink link;
    private boolean finished = false;

    public CraftingLinkState(MaintainerEntity owner, int slot, ICraftingLink link) {
        super(owner, slot);
        this.link = link;
    }

    @Nullable
    @Override
    public ProgressionState handle() {
        if (link.isDone()) {
            return new ExportSlotState(owner, slot);
        }

        if (link.isCanceled()) {
            return null;
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return finished ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    @Override
    public void interrupt() {
        finished = true;
    }

    public ICraftingLink getLink() {
        return link;
    }
}
