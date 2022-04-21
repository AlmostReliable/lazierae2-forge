package com.almostreliable.lazierae2.gui.control;

import com.almostreliable.lazierae2.gui.MaintainerScreen;
import com.almostreliable.lazierae2.gui.widgets.GenericButton;
import com.almostreliable.lazierae2.gui.widgets.ToggleButton;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.RequestBatchPacket;
import com.almostreliable.lazierae2.network.packets.RequestCountPacket;
import com.almostreliable.lazierae2.network.packets.RequestStatePacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public final class MaintainerControl {

    private static final int WIDGETS_PER_CONTROL = 6;
    public final MaintainerScreen screen;
    private final int slots;
    private final Control[] controls;

    public MaintainerControl(MaintainerScreen screen, int slots) {
        this.screen = screen;
        this.slots = slots;
        controls = new Control[slots];
        for (var slot = 0; slot < slots; slot++) {
            controls[slot] = new Control(slot);
        }
    }

    public AbstractWidget[] init() {
        var widgets = new AbstractWidget[slots * WIDGETS_PER_CONTROL];
        for (var control = 0; control < controls.length; control++) {
            var controlWidgets = controls[control].init();
            System.arraycopy(controlWidgets, 0, widgets, control * WIDGETS_PER_CONTROL, controlWidgets.length);
        }
        return widgets;
    }

    public void updateCountBox(int slot, long count) {
        var countBox = controls[slot].countBox;
        if (countBox == null) return;
        countBox.setValueFromLong(count);
    }

    public void updateBatchBox(int slot, long batch) {
        var batchBox = controls[slot].batchBox;
        if (batchBox == null) return;
        batchBox.setValueFromLong(batch);
    }

    private final class Control {

        // TODO: add tooltips for everything

        private static final int BUTTON_SIZE = 13;
        private final int slot;
        @Nullable
        private CountBox countBox;
        @Nullable
        private BatchBox batchBox;

        private Control(int slot) {
            this.slot = slot;
        }

        private AbstractWidget[] init() {
            countBox = new CountBox();
            batchBox = new BatchBox();
            var stateButton = new StateButton(screen);
            var progressionDisplay = new ProgressionDisplay();
            return new AbstractWidget[]{stateButton, countBox, countBox.submitButton, batchBox, batchBox.submitButton, progressionDisplay};
        }

        private final class ProgressionDisplay extends AbstractWidget {

            private static final int POS_X = 46;
            private static final int POS_Y = 20;
            private static final int WIDTH = 123;
            private static final int HEIGHT = 4;
            private static final int GAP = 16;

            private ProgressionDisplay() {
                super(
                    screen.getGuiLeft() + POS_X,
                    screen.getGuiTop() + slot * (GAP + HEIGHT) + POS_Y,
                    WIDTH,
                    HEIGHT,
                    TextComponent.EMPTY
                );
            }

            @Override
            public void renderButton(PoseStack stack, int mX, int mY, float partial) {
                fill(stack, x + 1, y + 1, x + WIDTH - 2, y + HEIGHT - 2, getProgressionColor());
            }

            @Override
            public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
                defaultButtonNarrationText(pNarrationElementOutput);
            }

            private int getProgressionColor() {
                if (!screen.getMenu().getRequestState(slot) || screen.getMenu().getRequestCount(slot) == 0) {
                    return 0xFF66_6666; // gray
                }
                var status = screen.getMenu().getProgressionType(slot);
                return switch (status) {
                    case IDLE -> 0xFF05_DA00; // green
                    case LINK -> 0xFFDA_8A00; // orange
                    case EXPORT -> 0xFF95_00DA; // purple
                    default -> throw new IllegalStateException("Impossible client state: " + status);
                };
            }
        }

        private abstract class TextBox extends EditBox {

            private static final int POS_Y = 7;
            private static final int WIDTH = 44;
            private static final int HEIGHT = 11;
            private static final int GAP = 9;
            final SubmitButton submitButton;

            @SuppressWarnings("AbstractMethodCallInConstructor")
            private TextBox(
                int x
            ) {
                super(
                    screen.getMinecraft().font,
                    screen.getGuiLeft() + x + 2,
                    screen.getGuiTop() + slot * (GAP + HEIGHT) + POS_Y + 2,
                    WIDTH,
                    HEIGHT,
                    TextComponent.EMPTY
                );
                submitButton = new SubmitButton(screen, x + WIDTH + 2, slot * (GAP + HEIGHT) + POS_Y - 1);
                setBordered(false);
                setTextColor(0xFF_FFFF);
                setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
                setMaxLength(6);
                setValue(String.valueOf(getServerValue()));
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                // submit value on enter
                if (keyCode == InputConstants.getKey("key.keyboard.enter").getValue()) {
                    validateAndSubmit();
                    setFocus(false);
                    screen.setFocused(null);
                    return true;
                }
                // submit value on tab and switch focus
                if (keyCode == InputConstants.getKey("key.keyboard.tab").getValue()) {
                    validateAndSubmit();
                    switchFocus();
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }

            protected abstract void switchFocus();

            protected abstract void syncValue();

            private void validateAndSubmit() {
                setValueFromLong(getValueAsLong());
            }

            long getValueAsLong() {
                try {
                    return Long.parseLong(getValue());
                } catch (NumberFormatException e) {
                    return getServerValue();
                }
            }

            protected abstract long getServerValue();

            @Override
            public boolean isHoveredOrFocused() {
                // don't allow tooltips on focus
                return isHovered;
            }

            void setValueFromLong(long value) {
                var oldValue = getServerValue();
                setValue(String.valueOf(value));
                if (value != oldValue) {
                    syncValue();
                }
            }

            private final class SubmitButton extends GenericButton {

                private static final String TEXTURE_ID = "submit";

                private SubmitButton(MaintainerScreen screen, int x, int y) {
                    super(screen, x, y, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_ID);
                }

                @Override
                public void renderButton(
                    PoseStack stack, int mX, int mY, float partial
                ) {
                    if (isHovered) {
                        RenderSystem.setShaderTexture(0, texture);
                        blit(stack, x, y, 0, BUTTON_SIZE, width, height, BUTTON_SIZE, getTextureHeight());
                    } else {
                        super.renderButton(stack, mX, mY, partial);
                    }
                }

                @Override
                protected void clickHandler() {
                    validateAndSubmit();
                }

                @Override
                protected int getTextureWidth() {
                    return BUTTON_SIZE;
                }

                @Override
                protected int getTextureHeight() {
                    return BUTTON_SIZE * 2;
                }
            }
        }

        private final class StateButton extends ToggleButton {

            private static final String TEXTURE_ID = "state";
            private static final int POS_X = 9;
            private static final int POS_Y = 9;
            private static final int GAP = 7;

            private StateButton(MaintainerScreen screen) {
                super(
                    screen,
                    POS_X,
                    slot * (GAP + BUTTON_SIZE) + POS_Y,
                    BUTTON_SIZE,
                    BUTTON_SIZE,
                    TEXTURE_ID,
                    () -> screen.getMenu().getRequestState(slot)
                );
            }

            @Override
            protected void clickHandler() {
                PacketHandler.CHANNEL.sendToServer(new RequestStatePacket(slot, !pressed.getAsBoolean()));
            }
        }

        private final class CountBox extends TextBox {

            private static final int POS_X = 47;

            private CountBox() {
                super(POS_X);
            }

            @Override
            protected void switchFocus() {
                var nextBox = controls[slot].batchBox;
                if (nextBox == null) return;
                setFocus(false);
                nextBox.setFocus(true);
                screen.setFocused(nextBox);
            }

            @Override
            protected void syncValue() {
                PacketHandler.CHANNEL.sendToServer(new RequestCountPacket(slot, getValueAsLong()));
            }

            @SuppressWarnings("AmbiguousFieldAccess")
            @Override
            protected long getServerValue() {
                return screen.getMenu().getRequestCount(slot);
            }
        }

        private final class BatchBox extends TextBox {

            private static final int POS_X = 110;

            private BatchBox() {
                super(POS_X);
            }

            @Override
            protected void switchFocus() {
                var nextBox = controls[slot + 1].countBox;
                if (nextBox == null) return;
                setFocus(false);
                nextBox.setFocus(true);
                screen.setFocused(nextBox);
            }

            @Override
            protected void syncValue() {
                PacketHandler.CHANNEL.sendToServer(new RequestBatchPacket(slot, getValueAsLong()));
            }

            @SuppressWarnings("AmbiguousFieldAccess")
            @Override
            protected long getServerValue() {
                return screen.getMenu().getRequestBatch(slot);
            }
        }
    }
}
