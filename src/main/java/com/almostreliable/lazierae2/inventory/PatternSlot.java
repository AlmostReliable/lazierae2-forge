package com.almostreliable.lazierae2.inventory;

import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import com.almostreliable.lazierae2.content.assembler.PatternHolderMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PatternSlot extends SlotItemHandler {

    private final PatternHolderMenu owner;

    public PatternSlot(PatternHolderMenu owner, IItemHandler itemHandler, int index, int pX, int pY) {
        super(itemHandler, index, pX, pY);
        this.owner = owner;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return AEItems.CRAFTING_PATTERN.isSameAs(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    public ItemStack getDisplayStack() {
        var stack = getItem();
        if (owner.entity.getLevel() != null && owner.entity.getLevel().isClientSide &&
            !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem pattern) {
            var patternOutput = pattern.getOutput(stack);
            if (!patternOutput.isEmpty()) return patternOutput;
        }
        return stack;
    }
}
