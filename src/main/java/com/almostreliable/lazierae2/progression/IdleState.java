package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.component.InventoryHandler;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public class IdleState implements ProgressionState {

    @Override
    public ProgressionState handle(MaintainerEntity owner, int slot) {
        if (owner.getCraftResults().getAmount(slot) > 0) {
            return ProgressionState.EXPORT_SLOT_STATE;
        }

        InventoryHandler.RequestInventory.Request request = owner.getCraftRequests().get(slot);
        if (request.isRequesting() && request.count() > owner.knownStorageAmounts[slot]) {
            return ProgressionState.REQUEST_CRAFT_STATE;
        }

        return this;
    }

    @Override
    public TickRateModulation getTickRateModulation() {
        return TickRateModulation.IDLE;
    }
}
