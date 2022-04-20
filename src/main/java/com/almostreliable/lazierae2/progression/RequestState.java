package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public class RequestState implements IProgressionState {

    @Override
    public IProgressionState handle(MaintainerEntity owner, int slot) {

        var craftRequests = owner.getCraftRequests();

        var toCraft = owner.getStorageManager().computeDelta(slot);
        if (toCraft <= 0) {
            return IProgressionState.IDLE_STATE;
        }

        var stack = craftRequests.get(slot).toGenericStack(toCraft);
        var future = owner
            .getMainNodeGrid()
            .getCraftingService()
            .beginCraftingCalculation(owner.getLevel(),
                owner::getActionSource,
                stack.what(),
                stack.amount(),
                CalculationStrategy.CRAFT_LESS
            );

        return new CraftingPlanState(future);
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SLOWER;
    }
}
