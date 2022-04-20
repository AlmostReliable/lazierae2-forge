package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;

public interface IProgressionState {

    IProgressionState IDLE_STATE = new IdleState();
    IProgressionState EXPORT_SLOT_STATE = new ExportState();
    IProgressionState REQUEST_CRAFT_STATE = new RequestState();

    /**
     * @param owner the entity that is maintaining the progression.
     * @param slot  the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    IProgressionState handle(MaintainerEntity owner, int slot);

    TickRateModulation getTickRateModulation();
}
