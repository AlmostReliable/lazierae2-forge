package com.almostreliable.lazierae2.inventory;

import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class FakeSlot extends SlotItemHandler {

    private final MaintainerMenu owner;

    public FakeSlot(MaintainerMenu owner, IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
        this.owner = owner;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
        }

        super.set(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }

    @NotNull
    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    public boolean isLocked() {
        return owner.entity.isRequestSlotLocked(getSlotIndex());
    }
}
