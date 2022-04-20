package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.GenericStack;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

import java.util.concurrent.Future;

public class RequestCraftState implements ProgressionState {

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {

        InventoryHandler.RequestInventory craftRequests = owner.getCraftRequests();

        long toCraft = owner.getStorageManager().computeDelta(slot);
        if (toCraft <= 0) {
            return ProgressionState.IDLE_STATE;
        }

        GenericStack stack = craftRequests.request(slot, (int) toCraft);
        Future<ICraftingPlan> future = owner
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
