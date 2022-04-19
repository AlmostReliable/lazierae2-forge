package com.almostreliable.lazierae2.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import appeng.helpers.externalstorage.GenericStackInv;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public class ExportSlotState implements ProgressionState {

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        GenericStackInv craftResults = owner.getCraftResults();
        GenericStack stack = craftResults.getStack(slot);
        if (stack == null) {
            return ProgressionState.IDLE_STATE;
        }

        long inserted = StorageHelper.poweredInsert(
            owner.getMainNodeGrid().getEnergyService(),
            owner.getMainNodeGrid().getStorageService().getInventory(),
            stack.what(),
            stack.amount(),
            owner.getActionSource(),
            Actionable.MODULATE
        );

        if (inserted > 0) {
            long remaining = stack.amount() - inserted;
            if (remaining > 0) {
                craftResults.setStack(slot, new GenericStack(stack.what(), remaining));
                return this;
            }

            craftResults.setStack(slot, null);
        }

        return ProgressionState.IDLE_STATE;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.URGENT;
    }
}
