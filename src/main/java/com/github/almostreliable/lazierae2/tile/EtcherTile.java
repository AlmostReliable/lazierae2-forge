package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.core.Setup.Tiles;

public class EtcherTile extends MachineTile {

    private static final int INPUT_SLOTS = 3;

    public EtcherTile() {
        super(Tiles.ETCHER.get(), INPUT_SLOTS);
    }
}
