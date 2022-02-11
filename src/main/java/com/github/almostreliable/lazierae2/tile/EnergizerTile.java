package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.container.EnergizerContainer;
import com.github.almostreliable.lazierae2.core.Setup.Tiles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nullable;

import static com.github.almostreliable.lazierae2.core.Constants.ENERGIZER_ID;

public class EnergizerTile extends MachineTile {

    private static final int INPUT_SLOTS = 1;

    public EnergizerTile() {
        super(Tiles.ENERGIZER.get(), ENERGIZER_ID, INPUT_SLOTS);
    }

    @Nullable
    @Override
    public Container createMenu(
        int id, PlayerInventory inventory, PlayerEntity player
    ) {
        return new EnergizerContainer(id, this);
    }
}
