package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CraftingPlanState implements ProgressionState {
    private final Future<ICraftingPlan> future;

    public CraftingPlanState(Future<ICraftingPlan> future) {
        this.future = future;
    }

    @Nullable
    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return ProgressionState.IDLE_STATE;
        }

        try {
            ICraftingPlan plan = future.get();
            ICraftingLink link = owner
                .getMainNodeGrid()
                .getCraftingService()
                .submitJob(plan, owner, null, false, owner.getActionSource());

            if (link == null) {
                return ProgressionState.IDLE_STATE;
            }

            return new CraftingLinkState(link);
        } catch (InterruptedException | ExecutionException e) {
            return ProgressionState.IDLE_STATE;
        }
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
