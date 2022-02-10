package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;

public class EtcherContainer extends MachineContainer {

    public EtcherContainer(
        int id, MachineTile tile
    ) {
        super(Containers.ETCHER.get(), id, tile);
    }
}
