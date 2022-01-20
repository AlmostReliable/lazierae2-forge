package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.core.Setup.Tiles;

public class CentrifugeTile extends MachineTile {

    private static final int INPUT_SLOTS = 1;

    public CentrifugeTile() {
        super(Tiles.CENTRIFUGE.get(), INPUT_SLOTS);
    }
}
