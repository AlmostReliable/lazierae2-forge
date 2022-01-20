package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.core.Setup.Tiles;

public class EnergizerTile extends MachineTile {

    private static final int INPUT_SLOTS = 1;

    public EnergizerTile() {
        super(Tiles.ENERGIZER.get(), INPUT_SLOTS);
    }
}
