package com.almostreliable.lazierae2.gui.widgets;

import com.almostreliable.lazierae2.core.TypeEnums.EXTRACT_SETTING;
import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.network.AutoExtractPacket;
import com.almostreliable.lazierae2.network.PacketHandler;
import com.almostreliable.lazierae2.network.packets.AutoExtractPacket;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.BooleanSupplier;

public class AutoExtractButton extends ToggleButton {

    private static final String TEXTURE_ID = "auto_extract";
    private static final int POS_X = 7;
    private static final int POS_Y = 28;
    private static final int BUTTON_SIZE = 18;
    private final Tooltip tooltip = setupTooltip();

    public AutoExtractButton(
        ProcessorScreen screen, BooleanSupplier pressed
    ) {
        super(screen, POS_X, POS_Y, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_ID, pressed);
    }

    @Override
    public void renderToolTip(PoseStack stack, int mX, int mY) {
        screen.renderComponentTooltip(stack, tooltip.build(), mX, mY);
    }

    @Override
    protected void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new AutoExtractPacket(!pressed.getAsBoolean()));
    }

    private Tooltip setupTooltip() {
        return Tooltip.builder().title("extract.title").blank().keyEnum(
            "extract.current",
            TRANSLATE_TYPE.EXTRACT_SETTING,
            () -> pressed.getAsBoolean() ? EXTRACT_SETTING.ON : EXTRACT_SETTING.OFF
        ).blank().line("extract.description").blank().clickAction("extract.action");
    }
}
