package com.github.almostreliable.lazierae2.tile;

import com.github.almostreliable.lazierae2.container.AggregatorContainer;
import com.github.almostreliable.lazierae2.core.Setup.Tiles;
import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.github.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

import static com.github.almostreliable.lazierae2.core.Constants.AGGREGATOR_ID;

public class AggregatorTile extends MachineTile {

    private static final int INPUT_SLOTS = 3;

    public AggregatorTile() {
        super(Tiles.AGGREGATOR.get(), INPUT_SLOTS);
    }

    @Nullable
    @Override
    public Container createMenu(
        int id, PlayerInventory inventory, PlayerEntity player
    ) {
        return new AggregatorContainer(id, this);
    }

    @Override
    public ITextComponent getDisplayName() {
        return TextUtil.translate(TRANSLATE_TYPE.CONTAINER, AGGREGATOR_ID);
    }
}
