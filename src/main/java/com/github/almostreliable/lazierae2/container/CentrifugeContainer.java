package com.github.almostreliable.lazierae2.container;

import com.github.almostreliable.lazierae2.core.Setup.Containers;
import com.github.almostreliable.lazierae2.tile.MachineTile;
import net.minecraft.entity.player.PlayerInventory;

public class CentrifugeContainer extends MachineContainer {

    public CentrifugeContainer(
        int id, MachineTile tile, PlayerInventory playerInventory
    ) {
        super(Containers.CENTRIFUGE.get(), id, tile, playerInventory);
    }
}
