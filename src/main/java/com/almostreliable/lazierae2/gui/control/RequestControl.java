package com.almostreliable.lazierae2.gui.control;

import com.almostreliable.lazierae2.core.TypeEnums.PROGRESSION_TYPE;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.gui.RequesterScreen;
import com.almostreliable.lazierae2.gui.widgets.GenericButton;
import com.almostreliable.lazierae2.gui.widgets.ToggleButton;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.RequestBatchPacket;
import com.almostreliable.lazierae2.network.packets.RequestCountPacket;
import com.almostreliable.lazierae2.network.packets.RequestStatePacket;
import com.almostreliable.lazierae2.util.GuiUtil;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class RequestControl {

    private static final int WIDGETS_PER_CONTROL = 6;
    public final RequesterScreen screen;
    private final int slots;
    private final Control[] controls;

    public RequestControl(RequesterScreen screen, int slots) {
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

    public void refreshControlBoxes(int slot) {
        var control = controls[slot];
        assert control.countBox != null && control.batchBox != null;
        control.countBox.setValueFromEntity();
        control.batchBox.setValueFromEntity();
    }

    private final class Control {

        private static final int BUTTON_SIZE = 13;
        private final int slot;
        @Nullable private CountBox countBox;
        @Nullable private BatchBox batchBox;

        private Control(int slot) {
            this.slot = slot;
        }

        private AbstractWidget[] init() {
            countBox = new CountBox();
            batchBox = new BatchBox();
            var stateButton = new StateBox(screen);
            var progressionDisplay = new ProgressionDisplay();
            return new AbstractWidget[]{stateButton, countBox, countBox.submitButton, batchBox, batchBox.submitButton, progressionDisplay};
        }

        private final class ProgressionDisplay extends AbstractWidget {

            private static final int POS_X = 46;
            private static final int POS_Y = 20;
            private static final int WIDTH = 123;
            private static final int HEIGHT = 4;
            private static final int GAP = 16;
            private final Tooltip tooltip;

            private ProgressionDisplay() {
                super(
                    screen.getGuiLeft() + POS_X,
                    screen.getGuiTop() + slot * (GAP + HEIGHT) + POS_Y,
                    WIDTH,
                    HEIGHT,
                    TextComponent.EMPTY
                );
                tooltip = setupTooltip();
            }

            @Override
            public void renderButton(PoseStack stack, int mX, int mY, float partial) {
                // noinspection ConstantConditions
                fill(
                    stack,
                    x + 1,
                    y + 1,
                    x + WIDTH - 2,
                    y + HEIGHT - 2,
                    GuiUtil.fillColorAlpha(getProgressionColor().getColor())
                );
            }

            @Override
            protected boolean isValidClickButton(int pButton) {
                return false;
            }

            @Override
            public void renderToolTip(PoseStack stack, int mX, int mY) {
                screen.renderComponentTooltip(stack, tooltip.build(), mX, mY);
            }

            @Override
            public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
                defaultButtonNarrationText(pNarrationElementOutput);
            }

            private Tooltip setupTooltip() {
                return Tooltip.builder()
                    .title("status.title")
                    .blank()
                    .conditional(current -> current.condition(() -> getProgressionColor() != ChatFormatting.DARK_GRAY)
                        .then(Tooltip.builder().keyEnum(
                            "status.current",
                            TRANSLATE_TYPE.REQUEST_STATUS,
                            () -> screen.getMenu().getRequestStatus(slot)
                        ))
                        .otherwise(Tooltip.builder().line("status.none", ChatFormatting.WHITE)))
                    .blank()
                    .conditional(advanced -> advanced.condition(Screen::hasShiftDown)
                        .then(Tooltip.builder().lineEnum(
                            TRANSLATE_TYPE.REQUEST_STATUS,
                            getProgressionColor(PROGRESSION_TYPE.IDLE),
                            PROGRESSION_TYPE.IDLE
                        ).line("status.idle").blank().lineEnum(
                            TRANSLATE_TYPE.REQUEST_STATUS,
                            getProgressionColor(PROGRESSION_TYPE.LINK),
                            PROGRESSION_TYPE.LINK
                        ).line("status.link").blank().lineEnum(
                            TRANSLATE_TYPE.REQUEST_STATUS,
                            getProgressionColor(PROGRESSION_TYPE.EXPORT),
                            PROGRESSION_TYPE.EXPORT
                        ).line("status.export"))
                        .otherwise(Tooltip.builder().shiftForInfo()));
            }

            private ChatFormatting getProgressionColor(PROGRESSION_TYPE status) {
                return switch (status) {
                    case IDLE -> ChatFormatting.DARK_GREEN;
                    case LINK -> ChatFormatting.YELLOW;
                    case EXPORT -> ChatFormatting.DARK_PURPLE;
                    default -> throw new IllegalStateException("Impossible client state: " + status);
                };
            }

            private ChatFormatting getProgressionColor() {
                if (!screen.getMenu().getRequestState(slot) || screen.getMenu().getRequestCount(slot) == 0) {
                    return ChatFormatting.DARK_GRAY;
                }
                var status = screen.getMenu().getRequestStatus(slot);
                return getProgressionColor(status);
            }
        }

        private abstract class TextBox extends EditBox {

            private static final int POS_Y = 7;
            private static final int WIDTH = 44;
            private static final int HEIGHT = 11;
            private static final int GAP = 9;
            final SubmitButton submitButton;
            final Tooltip tooltip;

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
                applyBoxDefaults();
                tooltip = Tooltip.builder();
                setupTooltip();
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

            protected void setupTooltip() {
                tooltip.blank()
                    .shiftForInfo()
                    .conditional(focused -> focused.condition(this::isFocused)
                        .then(Tooltip.builder()
                            .hotkeyAction("key.keyboard.tab", "focus.switch.action")
                            .hotkeyAction("key.keyboard.enter", "submit.action"))
                        .otherwise(Tooltip.builder().clickAction("focus.action")));
            }

            /**
             * Controls which element is focused next after tab is pressed.
             */
            protected abstract void switchFocus();

            protected abstract void sendToServer();

            protected void validateAndSubmit() {
                if (screen.getMenu().getRequestStack(slot).isEmpty()) {
                    setValueFromEntity();
                    return;
                }
                setValueFromLong(getValueAsLong());
            }

            /**
             * Sets the box value to the value that is stored in the block entity on
             * the respective side this method is called on.
             */
            void setValueFromEntity() {
                setValue(String.valueOf(getEntityValue()));
            }

            private void applyBoxDefaults() {
                setBordered(false);
                setTextColor(0xFF_FFFF);
                setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
                setMaxLength(6);
                setValueFromEntity();
            }

            long getValueAsLong() {
                try {
                    return Long.parseLong(getValue());
                } catch (NumberFormatException e) {
                    return getEntityValue();
                }
            }

            protected abstract long getEntityValue();

            @Override
            public boolean isHoveredOrFocused() {
                // override to disable showing tooltips on focus
                return isHovered;
            }

            @Override
            public void renderToolTip(PoseStack stack, int mX, int mY) {
                screen.renderComponentTooltip(stack, tooltip.build(), mX, mY);
            }

            private void setValueFromLong(long value) {
                var oldValue = getEntityValue();
                setValue(String.valueOf(value));
                if (value != oldValue) {
                    sendToServer();
                }
            }

            private final class SubmitButton extends GenericButton {

                private static final String TEXTURE_ID = "submit";
                private static final List<Component> TOOLTIP = Tooltip.builder().line(TEXTURE_ID).build();

                private SubmitButton(RequesterScreen screen, int x, int y) {
                    super(screen, x, y, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_ID);
                }

                @Override
                public void renderToolTip(PoseStack stack, int mX, int mY) {
                    screen.renderComponentTooltip(stack, TOOLTIP, mX, mY);
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

        private final class StateBox extends ToggleButton {

            private static final String TEXTURE_ID = "state";
            private static final int POS_X = 8;
            private static final int POS_Y = 8;
            private static final int BOX_SIZE = 14;
            private static final int GAP = 6;
            private static final List<Component> TOOLTIP = Tooltip.builder().line(TEXTURE_ID).build();

            private StateBox(RequesterScreen screen) {
                super(
                    screen,
                    POS_X,
                    slot * (GAP + BOX_SIZE) + POS_Y,
                    BOX_SIZE,
                    BOX_SIZE,
                    TEXTURE_ID,
                    () -> screen.getMenu().getRequestState(slot)
                );
            }

            @Override
            public void renderToolTip(PoseStack stack, int mX, int mY) {
                screen.renderComponentTooltip(stack, TOOLTIP, mX, mY);
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
            protected void setupTooltip() {
                tooltip.title("count.title")
                    .blank(Screen::hasShiftDown)
                    .line(Screen::hasShiftDown, "count.description");
                super.setupTooltip();
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
            protected void sendToServer() {
                PacketHandler.CHANNEL.sendToServer(new RequestCountPacket(slot, getValueAsLong()));
            }

            @Override
            protected long getEntityValue() {
                return screen.getMenu().getRequestCount(slot);
            }
        }

        private final class BatchBox extends TextBox {

            private static final int POS_X = 110;

            private BatchBox() {
                super(POS_X);
            }

            @Override
            protected void setupTooltip() {
                tooltip.title("batch.title")
                    .blank(Screen::hasShiftDown)
                    .line(Screen::hasShiftDown, "batch.description");
                super.setupTooltip();
            }

            @Override
            protected void switchFocus() {
                var nextBox = controls[slot == controls.length - 1 ? 0 : slot + 1].countBox;
                if (nextBox == null) return;
                setFocus(false);
                nextBox.setFocus(true);
                screen.setFocused(nextBox);
            }

            @Override
            protected void sendToServer() {
                PacketHandler.CHANNEL.sendToServer(new RequestBatchPacket(slot, getValueAsLong()));
            }

            @Override
            protected void validateAndSubmit() {
                if (getValueAsLong() <= 0) return;
                super.validateAndSubmit();
            }

            @Override
            protected long getEntityValue() {
                return screen.getMenu().getRequestBatch(slot);
            }
        }
    }
}
