package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;

public interface IProgressionState {

    IProgressionState IDLE = new IdleState();
    IProgressionState REQUEST = new RequestState();
    IProgressionState EXPORT = new ExportState();

    /**
     * @param owner the entity that is maintaining the progression.
     * @param slot  the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    IProgressionState handle(RequesterEntity owner, int slot);

    PROGRESSION_TYPE type();

    TickRateModulation getTickRateModulation();
}
