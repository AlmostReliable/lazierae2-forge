package com.almostreliable.lazierae2.content.assembler.holder;

import appeng.core.definitions.AEItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PatternInventory extends ItemStackHandler {

    private final PatternHolderEntity entity;

    PatternInventory(PatternHolderEntity entity) {
        super(calculateSize(entity));
        this.entity = entity;
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
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return AEItems.CRAFTING_PATTERN.isSameAs(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        entity.setChanged();
        // entity.updatePatterns();
        // TODO: notify controller to update patterns
    }

    void dropContents() {
        if (entity.getLevel() == null) return;
        var pos = entity.getBlockPos();
        for (var slot = 0; slot < getSlots(); slot++) {
            var stack = getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            entity.getLevel()
                .addFreshEntity(new ItemEntity(entity.getLevel(), pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }
}
