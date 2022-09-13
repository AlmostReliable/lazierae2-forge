package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.assembler.controller.ControllerMenu;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.inventory.PatternReferenceSlot;
import com.almostreliable.lazierae2.util.GuiUtil;
import com.almostreliable.lazierae2.util.GuiUtil.ANCHOR;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Blocks.CONTROLLER_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ControllerScreen extends GenericScreen<ControllerMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL(f("textures/gui/{}.png", CONTROLLER_ID));
    private static final int TEXTURE_WIDTH = 194;
    private static final int TEXTURE_HEIGHT = 231;
    private static final int OVERLAYS_HEIGHT = 36;
    private static final int SLOT_SIZE = 18;
    private static final int SLIDER_WIDTH = 12;
    private static final int SLIDER_HEIGHT = 15;
    private static final int SCROLLBAR_X = 174;
    private static final int SCROLLBAR_Y = 8;
    private static final int SCROLLBAR_HEIGHT = 85;
    private static final List<Component> EXCLAMATION_TOOLTIP = Tooltip.builder()
        .line("invalid_patterns.header", ChatFormatting.DARK_RED)
        .blank()
        .line("invalid_patterns.description")
        .build();

    private int scrollOffset;
    private boolean scrolling;

    @SuppressWarnings("AssignmentToSuperclassField")
    public ControllerScreen(
        ControllerMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT - OVERLAYS_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mX, double mY, double delta) {
        if (canScroll()) {
            if (delta > 0 && scrollOffset <= 0) return true;
            var rows = menu.controllerData.getSlots() / ControllerMenu.COLUMNS;
            if (delta < 0 && rows - ControllerMenu.ROWS <= scrollOffset) return true;
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
        // title
        drawCenteredString(
            stack,
            font,
            TextUtil.translateAsString(TRANSLATE_TYPE.GUI, CONTROLLER_ID),
            TEXTURE_WIDTH / 2,
            -12,
            0xFFFF_FFFF
        );
        // no pattern holders
        if (calculateRowsToDraw() == 0) {
            var text = TextUtil.translateAsString(TRANSLATE_TYPE.GUI, "controller.empty");
            GuiUtil.renderText(stack, text, ANCHOR.CENTER, (TEXTURE_WIDTH - 20) / 2, 45, 1.0f, 0xFF55_5555);
        }

        // TODO: remove these debug labels
        drawString(stack, font, "accelerators: " + menu.getAccelerators(), 79, 94, 0xFFFF_FFFF);
        drawString(stack, font, "work: " + menu.getWork(), 79, 102, 0xFFFF_FFFF);
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
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT - OVERLAYS_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );

        // scrollbar
        var x = leftPos + SCROLLBAR_X;
        var y = topPos + SCROLLBAR_Y;
        var offscreenRows = menu.controllerData.getSlots() / ControllerMenu.COLUMNS - ControllerMenu.ROWS;
        var offset = offscreenRows <= 0 ? 0 : scrollOffset * (SCROLLBAR_HEIGHT - SLIDER_HEIGHT) / offscreenRows;
        blit(
            stack,
            x,
            y + offset,
            162f + (canScroll() ? 0 : SLIDER_WIDTH),
            195,
            SLIDER_WIDTH,
            SLIDER_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );

        // slots
        for (var row = 0; row < calculateRowsToDraw(); row++) {
            var vOffset = 195f + (isInvalidRow(row) ? SLOT_SIZE : 0);
            blit(
                stack,
                leftPos + 7,
                topPos + 7 + row * SLOT_SIZE,
                0,
                vOffset,
                SLOT_SIZE * ControllerMenu.COLUMNS,
                SLOT_SIZE,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
            );
        }

        // exclamation mark
        if (!menu.controllerData.invalidRows.isEmpty()) {
            blit(stack, leftPos - 5, topPos - 5, 162, 212, 12, 12, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        renderCraftingMatrix();
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

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        super.renderTooltip(stack, mX, mY);

        if (isHovering(-5, -5, 12, 12, mX, mY) && !menu.controllerData.invalidRows.isEmpty()) {
            renderComponentTooltip(stack, EXCLAMATION_TOOLTIP, mX, mY);
        }
    }

    private int calculateRowsToDraw() {
        return Mth.clamp(menu.controllerData.getSlots() / ControllerMenu.COLUMNS, 0, ControllerMenu.ROWS);
    }

    // Mojang uses a different render stack for rendering items inside GUIs, I don't question it anymore
    private void renderCraftingMatrix() {
        var craftingMatrix = menu.getCraftingMatrix();
        var renderStack = RenderSystem.getModelViewStack();

        // input grid
        renderStack.pushPose();
        renderStack.translate(leftPos + 7.0, topPos + 82.0, 0);
        renderStack.scale(0.5f, 0.5f, 0.5f);
        for (var row = 0; row < 3; row++) {
            for (var col = 0; col < 3; col++) {
                itemRenderer.renderAndDecorateFakeItem(craftingMatrix[row * 3 + col], col * SLOT_SIZE, row * SLOT_SIZE);
            }
        }
        renderStack.popPose();

        // output slot
        itemRenderer.renderAndDecorateFakeItem(craftingMatrix[9], leftPos + 36, topPos + 83);
    }

    private boolean isInvalidRow(int row) {
        return !menu.controllerData.invalidRows.isEmpty() && menu.controllerData.invalidRows.contains(row + scrollOffset);
    }

    private boolean canScroll() {
        return menu.controllerData.getSlots() > ControllerMenu.ROWS * ControllerMenu.COLUMNS;
    }

    @SuppressWarnings("ConstantConditions")
    private void performScroll() {
        for (var slot = menu.controllerData.getSlots() + GenericMenu.PLAYER_INV_SIZE; slot < menu.slots.size(); slot++) {
            if (menu.slots.get(slot) instanceof PatternReferenceSlot reference) {
                reference.setRow(scrollOffset);
            }
        }
    }
}
