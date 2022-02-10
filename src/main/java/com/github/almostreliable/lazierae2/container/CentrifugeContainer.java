package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;

public class CentrifugeContainer extends MachineContainer {

    public CentrifugeContainer(
        int id, MachineTile tile
    ) {
        super(Containers.CENTRIFUGE.get(), id, tile);
    }
}
