package com.almostreliable.lazierae2.gui.widgets;

import com.almostreliable.lazierae2.gui.MaintainerScreen;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.RequestBatchPacket;
import com.almostreliable.lazierae2.network.packets.RequestCountPacket;
import com.almostreliable.lazierae2.network.packets.RequestStatePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public final class MaintainerControl {

    // TODO: add tooltips for everything

    private static final int POS_Y = 9;
    private static final int GAP = 10;
    private final MaintainerScreen screen;
    private final int slot;
    private CountBox countBox;
    private BatchBox batchBox;

    private MaintainerControl(MaintainerScreen screen, int slot) {
        this.screen = screen;
        this.slot = slot;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static MaintainerControl[] create(MaintainerScreen screen, int slotCount) {
        var controls = new MaintainerControl[slotCount];
        for (var i = 0; i < slotCount; i++) {
            controls[i] = new MaintainerControl(screen, i);
        }
        return controls;
    }

    public void updateCountBox(long count) {
        countBox.changeValue(count);
    }

    public void updateBatchBox(long batch) {
        batchBox.changeValue(batch);
    }

    public AbstractWidget[] createWidgets() {
        countBox = new CountBox(screen, slot);
        batchBox = new BatchBox(screen, slot);
        return new AbstractWidget[]{new StateButton(screen,
            slot
        ), countBox, countBox.submitButton, batchBox, batchBox.submitButton};
    }

    private abstract static class TextField extends EditBox {

        private static final int WIDTH = 44;
        private static final int HEIGHT = 11;
        final MaintainerScreen screen;
        final SubmitButton submitButton;

        @SuppressWarnings("AbstractMethodCallInConstructor")
        private TextField(
            MaintainerScreen screen, int slot, int x
        ) {
            super(screen.getMinecraft().font,
                screen.getGuiLeft() + x,
                screen.getGuiTop() + slot * 2 * GAP + POS_Y + 2,
                WIDTH,
                HEIGHT,
                TextComponent.EMPTY
            );
            this.screen = screen;
            submitButton = new SubmitButton(screen, slot, x + WIDTH);
            setBordered(false);
            setTextColor(0xFF_FFFF);
            setFilter(text -> StringUtils.isNumeric(text) || text.isEmpty());
            setMaxLength(6);
            setValue(String.valueOf(getOldValue()));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            // submit text field on enter
            if (keyCode == InputConstants.getKey("key.keyboard.enter").getValue()) {
                validate();
                syncValue();
                setFocus(false);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        protected abstract void syncValue();

        void changeValue(long value) {
            var oldValue = getValue();
            var newValue = String.valueOf(value);
            setValue(newValue);
            if (!newValue.equals(oldValue)) {
                syncValue();
            }
        }

        private void validate() {
            var oldValue = getOldValue();
            long value;
            try {
                value = Long.parseLong(getValue());
            } catch (NumberFormatException e) {
                setValue(String.valueOf(oldValue));
                return;
            }
            changeValue(value);
        }

        long getNumberValue() {
            return Long.parseLong(getValue());
        }

        protected abstract long getOldValue();

        @Override
        public boolean isHoveredOrFocused() {
            // don't allow tooltips on focus
            return isHovered;
        }

        private final class SubmitButton extends GenericButton {

            // TODO: make a hover effect with another texture

            private static final String TEXTURE_ID = "submit";
            private static final int BUTTON_SIZE = 13;

            private SubmitButton(MaintainerScreen screen, int slot, int x) {
                super(screen, x, slot * 2 * GAP + POS_Y, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_ID);
            }

            @Override
            protected void clickHandler() {
                validate();
                syncValue();
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

        // TODO: make this a checkbox instead

        private static final String TEXTURE_ID = "state";
        private static final int POS_X = 9;
        private static final int BUTTON_SIZE = 13;

        private StateButton(MaintainerScreen screen, int slot) {
            super(screen,
                POS_X,
                slot * 2 * GAP + POS_Y,
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

    private final class CountBox extends TextField {

        private static final int POS_X = 49;

        private CountBox(MaintainerScreen screen, int slot) {
            super(screen, slot, POS_X);
        }

        @Override
        protected void syncValue() {
            PacketHandler.CHANNEL.sendToServer(new RequestCountPacket(slot, getNumberValue()));
        }

        @SuppressWarnings("AmbiguousFieldAccess")
        @Override
        protected long getOldValue() {
            return screen.getMenu().getRequestCount(slot);
        }
    }

    private final class BatchBox extends TextField {

        private static final int POS_X = 112;

        private BatchBox(MaintainerScreen screen, int slot) {
            super(screen, slot, POS_X);
        }

        @Override
        protected void syncValue() {
            PacketHandler.CHANNEL.sendToServer(new RequestBatchPacket(slot, getNumberValue()));
        }

        @SuppressWarnings("AmbiguousFieldAccess")
        @Override
        protected long getOldValue() {
            return screen.getMenu().getRequestBatch(slot);
        }
    }
}
