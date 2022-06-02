package com.almostreliable.lazierae2.content.assembler;

import net.minecraftforge.items.ItemStackHandler;

public class PatternInventory extends ItemStackHandler {

    private final PatternHolderEntity entity;
    private final int size;

    PatternInventory(PatternHolderEntity entity) {
        this.entity = entity;
        size = calculateSize(entity);
    }

    private static int calculateSize(PatternHolderEntity entity) {
        return switch (entity.getTier()) {
            case TIER_1 -> 9;
            case TIER_2 -> 18;
            case TIER_3 -> 27;
            case ACCELERATOR -> throw new IllegalStateException("Accelerator tier has no inventory");
        };
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        entity.setChanged();
    }
}
