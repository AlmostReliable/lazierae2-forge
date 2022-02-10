package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;

public class AggregatorContainer extends MachineContainer {

    public AggregatorContainer(
        int id, MachineTile tile
    ) {
        super(Containers.AGGREGATOR.get(), id, tile);
    }
}
