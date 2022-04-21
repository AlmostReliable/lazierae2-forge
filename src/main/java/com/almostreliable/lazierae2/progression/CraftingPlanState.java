package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class CraftingPlanState extends ProgressionState {

    private final Future<? extends ICraftingPlan> future;

    CraftingPlanState(Future<? extends ICraftingPlan> future) {
        super(PROGRESSION_TYPE.PLAN);
        this.future = future;
    }

    @SuppressWarnings("java:S2142")
    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return new IdleState();
        }

        try {
            var plan = future.get();
            var link = owner
                .getMainNodeGrid()
                .getCraftingService()
                .submitJob(plan, owner, null, false, owner.getActionSource());

            if (link == null) {
                return new IdleState();
            }

            return new CraftingLinkState(link);
        } catch (InterruptedException | ExecutionException e) {
            return new IdleState();
        }
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return future.isDone() && !future.isCancelled() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }
}
