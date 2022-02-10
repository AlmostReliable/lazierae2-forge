package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;

public class EnergizerContainer extends MachineContainer {

    public EnergizerContainer(
        int id, MachineTile tile
    ) {
        super(Containers.ENERGIZER.get(), id, tile);
    }
}
