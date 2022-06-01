package com.almostreliable.lazierae2.compat.jei;

import com.almostreliable.lazierae2.gui.RequesterScreen;
import com.almostreliable.lazierae2.inventory.FakeSlot;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.RequestStackPacket;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RequesterGhostHandler implements IGhostIngredientHandler<RequesterScreen> {

    @Override
    public <I> List<Target<I>> getTargets(RequesterScreen gui, I ingredient, boolean doStart) {
        if (!(ingredient instanceof ItemStack)) {
            return List.of();
        }
        return resolveItemTargets(gui);
    }

    @Override
    public void onComplete() {
        // ignored
    }

    @SuppressWarnings("unchecked")
    private <I> List<Target<I>> resolveItemTargets(RequesterScreen gui) {
        List<Target<I>> targets = new ArrayList<>();
        for (var slot : gui.getMenu().slots) {
            if (slot instanceof FakeSlot fakeSlot && !fakeSlot.isLocked()) {
                targets.add((Target<I>) new SlotTarget(gui, fakeSlot));
            }
        }
        return targets;
    }

    @SuppressWarnings("NewExpressionSideOnly")
    private static final class SlotTarget implements Target<ItemStack> {

        private final FakeSlot slot;
        private final Rect2i area;

        private SlotTarget(RequesterScreen screen, FakeSlot slot) {
            this.slot = slot;
            area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(ItemStack stack) {
            slot.set(stack);
            PacketHandler.CHANNEL.sendToServer(new RequestStackPacket(slot.getSlotIndex(), stack));
        }
    }
}
