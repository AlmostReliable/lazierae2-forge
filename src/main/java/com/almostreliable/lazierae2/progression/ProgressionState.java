package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public interface ProgressionState {
    ProgressionState IDLE_STATE = new IdleState();
    ProgressionState EXPORT_SLOT_STATE = new ExportSlotState();
    ProgressionState REQUEST_CRAFT_STATE = new RequestCraftState();

    /**
     * @param owner the entity that is maintaining the progression.
     * @param slot  the slot of the progression.
     * @return the next state, self, or null if the progression is complete.
     */
    ProgressionState handle(MaintainerEntity owner, int slot);

    TickRateModulation getTickRateModulation();
}
