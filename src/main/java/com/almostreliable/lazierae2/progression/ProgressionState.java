package com.almostreliable.lazierae2.progression;

import appeng.api.networking.ticking.TickRateModulation;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ProgressionState {

    private PROGRESSION_TYPE type;

    ProgressionState(PROGRESSION_TYPE type) {
        this.type = type;
    }

    /**
     * @param owner the entity that is maintaining the progression.
     * @param slot  the slot of the progression.
     * @return the next state, self, or idle if the progression is complete.
     */
    public abstract ProgressionState handle(MaintainerEntity owner, int slot);

    public abstract TickRateModulation getTickRateModulation();

    public PROGRESSION_TYPE getType() {
        return type;
    }

    @OnlyIn(Dist.CLIENT)
    public void setType(PROGRESSION_TYPE type) {
        this.type = type;
    }
}
