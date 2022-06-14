package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.assembler.controller.ControllerMenu;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.inventory.PatternReferenceSlot;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import static com.almostreliable.lazierae2.core.Constants.Blocks.CONTROLLER_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ControllerScreen extends GenericScreen<ControllerMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL(f("textures/gui/{}.png", CONTROLLER_ID));
    private static final int TEXTURE_WIDTH = 219;
    private static final int TEXTURE_HEIGHT = 200;
    private static final int OVERLAYS_WIDTH = 24;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;
    private static final int SCROLLBAR_X = 175;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_HEIGHT = 85;
    private int scrollOffset;
    private boolean scrolling;
    // private static final int SLOT_SIZE = 18;

    @SuppressWarnings("AssignmentToSuperclassField")
    public ControllerScreen(
        ControllerMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        imageWidth = TEXTURE_WIDTH - OVERLAYS_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mX, double mY, double delta) {
        if (canScroll()) {
            var rows = menu.controllerData.getSlots() / ControllerMenu.COLUMNS;
            var offset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, rows - ControllerMenu.ROWS);
            if (offset != scrollOffset) {
                scrollOffset = offset;
                performScroll();
            }
            return true;
        }
        return super.mouseScrolled(mX, mY, delta);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mX, int mY) {
        drawCenteredString(
            stack,
            font,
            TextUtil.translateAsString(TRANSLATE_TYPE.GUI, CONTROLLER_ID),
            (TEXTURE_WIDTH - OVERLAYS_WIDTH) / 2,
            -12,
            0xFFFF_FFFF
        );

        drawString(stack, font, "scrollOffset: " + scrollOffset, 79, 96, 0xFFFF_FFFF);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(
            stack,
            leftPos,
            topPos,
            0,
            0,
            TEXTURE_WIDTH - OVERLAYS_WIDTH,
            TEXTURE_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );

        // TODO: render slots

        // scrollbar
        var x = leftPos + SCROLLBAR_X;
        var y = topPos + SCROLLBAR_Y;
        var offset = scrollOffset * (SCROLLBAR_HEIGHT - SLIDER_HEIGHT) / (menu.controllerData.getSlots() / ControllerMenu.COLUMNS - ControllerMenu.ROWS);
        blit(
            stack,
            x,
            y + offset,
            195f + (canScroll() ? 0 : SLIDER_WIDTH),
            2f,
            SLIDER_WIDTH,
            SLIDER_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );
    }

    @Override
    public boolean mouseClicked(double mX, double mY, int button) {
        if (button == 0 && isHovering(SCROLLBAR_X, SCROLLBAR_Y, SLIDER_WIDTH, SCROLLBAR_HEIGHT, mX, mY)) {
            scrolling = canScroll();
            return true;
        }
        return super.mouseClicked(mX, mY, button);
    }

    @Override
    public boolean mouseDragged(double mX, double mY, int button, double dragX, double dragY) {
        if (scrolling) {
            var upperBound = topPos + SCROLLBAR_Y;
            var percentage = ((float) mY - upperBound - SLIDER_HEIGHT / 2f) / (SCROLLBAR_HEIGHT - SLIDER_HEIGHT);
            percentage = Mth.clamp(percentage, 0f, 1f);
            var newOffset = (int) (percentage * ((float) menu.controllerData.getSlots() / ControllerMenu.COLUMNS - ControllerMenu.ROWS));
            if (scrollOffset != newOffset) {
                scrollOffset = newOffset;
                performScroll();
            }
            return true;
        }
        return super.mouseDragged(mX, mY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mX, double mY, int button) {
        if (button == 0) scrolling = false;
        return super.mouseReleased(mX, mY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType type) {
        if (slot instanceof PatternReferenceSlot) slot.index = slot.getContainerSlot();
        super.slotClicked(slot, slotId, button, type);
    }

    private boolean canScroll() {
        return menu.controllerData.getSlots() > ControllerMenu.ROWS * ControllerMenu.COLUMNS;
    }

    private void performScroll() {
        var start = menu.controllerData.getSlots() - 1;
        var end = menu.slots.size();
        for (var slot = start; slot < end; slot++) {
            if (menu.slots.get(slot) instanceof PatternReferenceSlot reference) {
                reference.setRow(scrollOffset);
            }
        }
    }
}
