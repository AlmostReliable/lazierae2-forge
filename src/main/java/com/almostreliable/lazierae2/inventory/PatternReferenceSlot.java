package com.almostreliable.lazierae2.inventory;

import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class PatternReferenceSlot extends Slot {

    private static final Container EMPTY_INVENTORY = new SimpleContainer(0);
    private final ControllerMenu owner;
    private final IItemHandlerModifiable itemHandler;
    private int reference;

    public PatternReferenceSlot(
        ControllerMenu owner, IItemHandlerModifiable itemHandler, int reference, int pX, int pY
    ) {
        super(EMPTY_INVENTORY, reference, pX, pY);
        this.owner = owner;
        this.itemHandler = itemHandler;
        this.reference = reference;
    }

    @Override
    public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {
        // empty to overwrite the default behaviour
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && AEItems.CRAFTING_PATTERN.isSameAs(stack);
    }

    @Override
    @Nonnull
    public ItemStack getItem() {
        return itemHandler.getStackInSlot(reference);
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        itemHandler.setStackInSlot(reference, stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 1;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        return itemHandler.extractItem(reference, amount, false);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !itemHandler.extractItem(reference, 1, true).isEmpty();
    }

    @Override
    public int getContainerSlot() {
        return reference;
    }

    public ItemStack getDisplayStack() {
        var stack = getItem();
        if (owner.entity.getLevel() != null && owner.entity.getLevel().isClientSide && !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem pattern) {
            var patternOutput = pattern.getOutput(stack);
            if (!patternOutput.isEmpty()) return patternOutput;
        }
        return stack;
    }

    public void setRow(int row) {
        reference = getSlotIndex() + row * ControllerMenu.COLUMNS;
    }
}
