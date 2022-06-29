package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public class RequestState implements IProgressionState {

    RequestState() {}

    @Override
    public IProgressionState handle(RequesterEntity owner, int slot) {

        var craftRequests = owner.craftRequests;

        var toCraft = owner.getStorageManager().computeDelta(slot);
        if (toCraft <= 0) {
            return IProgressionState.IDLE;
        }

        var stack = craftRequests.get(slot).toGenericStack(toCraft);
        var future = owner.getGrid()
            .getCraftingService()
            .beginCraftingCalculation(
                owner.getLevel(),
                owner::getActionSource,
                stack.what(),
                stack.amount(),
                CalculationStrategy.CRAFT_LESS
            );

        return new CraftingPlanState(future);
    }

    @Override
    public PROGRESSION_TYPE type() {
        return PROGRESSION_TYPE.REQUEST;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.SLOWER;
    }
}
