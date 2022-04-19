package com.almostreliable.lazierae2.progression;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.GenericStack;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.Future;

public class RequestCraftState implements ProgressionState {

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (owner.knownStorageAmounts[slot] == -1) {
            // todo: throw out into owner
            ItemStack stack = owner.craftRequests.get(slot).stack();
            if (stack.isEmpty()) return this;
            GenericStack genericStack = GenericStack.fromItemStack(stack);
            if (genericStack == null) return this;
            owner.knownStorageAmounts[slot] = owner
                .getMainNodeGrid()
                .getStorageService()
                .getInventory()
                .getAvailableStacks()
                .get(genericStack.what());
        }

        InventoryHandler.RequestInventory craftRequests = owner.getCraftRequests();

        long toCraft = craftRequests.computeDelta(slot, owner.knownStorageAmounts[slot]);
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
        return TickRateModulation.URGENT;
    }
}
