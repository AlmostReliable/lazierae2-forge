package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.container.EtcherContainer;
import com.github.almostreliable.lazierae2.core.Setup.Tiles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;

import javax.annotation.Nullable;

import static com.github.almostreliable.lazierae2.core.Constants.ETCHER_ID;

public class EtcherTile extends MachineTile {

    private static final int INPUT_SLOTS = 3;

    public EtcherTile() {
        super(Tiles.ETCHER.get(), ETCHER_ID, INPUT_SLOTS);
    }

    @Nullable
    @Override
    public Container createMenu(
        int id, PlayerInventory inventory, PlayerEntity player
    ) {
        return new EtcherContainer(id, this, inventory);
    }
}
