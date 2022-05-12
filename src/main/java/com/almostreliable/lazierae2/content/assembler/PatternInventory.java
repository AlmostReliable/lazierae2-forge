package com.almostreliable.lazierae2.content.assembler;

import net.minecraftforge.items.ItemStackHandler;

public class PatternInventory extends ItemStackHandler {

    private final CenterEntity entity;

    PatternInventory(CenterEntity entity) {
        this.entity = entity;
    }

    private static int calculateSize(CenterEntity entity) {
        return switch (entity.getProcessorType()) {
            case ACCELERATOR -> 0;
            case TIER_1 -> 9;
            case TIER_2 -> 18;
            case TIER_3 -> 27;
        };
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        entity.setChanged();
    }
}
