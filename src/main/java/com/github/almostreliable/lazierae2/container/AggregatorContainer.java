package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;
import net.minecraft.entity.player.PlayerInventory;

public class AggregatorContainer extends MachineContainer {

    public AggregatorContainer(
        int id, MachineTile tile, PlayerInventory playerInventory
    ) {
        super(Containers.AGGREGATOR.get(), id, tile, playerInventory);
    }
}
