package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.container.CentrifugeContainer;
import com.github.almostreliable.lazierae2.core.Setup.Tiles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nullable;

import static com.github.almostreliable.lazierae2.core.Constants.CENTRIFUGE_ID;

public class CentrifugeTile extends MachineTile {

    private static final int INPUT_SLOTS = 1;

    public CentrifugeTile() {
        super(Tiles.CENTRIFUGE.get(), CENTRIFUGE_ID, INPUT_SLOTS);
    }

    @Nullable
    @Override
    public Container createMenu(
        int id, PlayerInventory inventory, PlayerEntity player
    ) {
        return new CentrifugeContainer(id, this);
    }
}
