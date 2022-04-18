package com.almostreliable.lazierae2.progression;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import appeng.helpers.externalstorage.GenericStackInv;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import org.jetbrains.annotations.Nullable;

public class ExportSlotState extends MaintainerProgressionState {
    public ExportSlotState(MaintainerEntity owner, int slot) {
        super(owner, slot);
    }

    @Nullable
    @Override
    public ProgressionState handle() {
        GenericStackInv craftResults = owner.getCraftResults();
        GenericStack stack = craftResults.getStack(slot);
        if (stack == null) {
            return null;
        }

        long inserted = StorageHelper.poweredInsert(
            grid.getEnergyService(),
            grid.getStorageService().getInventory(),
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

        return null;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.FASTER;
    }
}
