package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CraftingPlanState extends MaintainerProgressionState {
    private final Future<ICraftingPlan> future;

    public CraftingPlanState(
        MaintainerEntity owner, int slot, Future<ICraftingPlan> future
    ) {
        super(owner, slot);
        this.future = future;
    }

    @Nullable
    @Override
    public ProgressionState handle() {
        if (!future.isDone()) {
            return this;
        }

        if (future.isCancelled()) {
            return null;
        }

        try {
            ICraftingPlan plan = future.get();
            ICraftingLink link = grid
                .getCraftingService()
                .submitJob(plan, owner, null, false, owner.getActionSource());

            if(link == null) {
                return null;
            }

            return new CraftingLinkState(owner, slot, link);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT; //TODO
    }
}
