package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public record CraftingPlanState(Future<? extends ICraftingPlan> future) implements IProgressionState {

    @SuppressWarnings("java:S2142")
    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return IProgressionState.IDLE_STATE;
        }

        try {
            var plan = future.get();
            var link = owner
                .getMainNodeGrid()
                .getCraftingService()
                .submitJob(plan, owner, null, false, owner.getActionSource());

            if (link == null) {
                return IProgressionState.IDLE_STATE;
            }

            return new CraftingLinkState(link);
        } catch (InterruptedException | ExecutionException e) {
            return IProgressionState.IDLE_STATE;
        }
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
