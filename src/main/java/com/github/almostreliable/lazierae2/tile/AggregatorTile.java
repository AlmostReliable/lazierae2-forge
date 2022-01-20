package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.core.Setup.Tiles;

public class AggregatorTile extends MachineTile {

    private static final int INPUT_SLOTS = 3;

    public AggregatorTile() {
        super(Tiles.AGGREGATOR.get(), INPUT_SLOTS);
    }
}
