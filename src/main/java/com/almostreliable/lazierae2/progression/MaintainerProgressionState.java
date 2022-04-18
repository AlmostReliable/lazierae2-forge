package com.almostreliable.lazierae2.progression;

import appeng.api.networking.IGrid;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

import java.util.Objects;

public abstract class MaintainerProgressionState implements ProgressionState {
    protected final MaintainerEntity owner;
    protected final int slot;
    protected final IGrid grid;

    public MaintainerProgressionState(MaintainerEntity owner, int slot) {
        this.owner = owner;
        this.slot = slot;
        this.grid = owner.getMainNode().getGrid();
        Objects.requireNonNull(grid, "Grid was not initialized correctly");
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public void interrupt() {
        // nothing to do
    }
}
