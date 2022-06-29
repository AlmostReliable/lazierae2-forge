package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class CraftingPlanState implements IProgressionState {

    private final Future<? extends ICraftingPlan> future;

    CraftingPlanState(Future<? extends ICraftingPlan> future) {
        this.future = future;
    }

    @SuppressWarnings("java:S2142")
    @Override
    public IProgressionState handle(RequesterEntity owner, int slot) {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return IProgressionState.IDLE;
        }

        try {
            var plan = future.get();
            var link = owner.getGrid()
                .getCraftingService()
                .submitJob(plan, owner, null, false, owner.getActionSource());

            if (link == null) {
                return IProgressionState.IDLE;
            }

            return new CraftingLinkState(link);
        } catch (InterruptedException | ExecutionException e) {
            return IProgressionState.IDLE;
        }
    }

    @Override
    public PROGRESSION_TYPE type() {
        return PROGRESSION_TYPE.PLAN;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
