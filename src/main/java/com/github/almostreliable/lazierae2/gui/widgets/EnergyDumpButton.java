package com.github.almostreliable.lazierae2.gui.widgets;

import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.network.EnergyDumpPacket;
import com.github.almostreliable.lazierae2.network.PacketHandler;
import com.github.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;

public class EnergyDumpButton extends GenericButton {

    private static final String TEXTURE_ID = "dump";
    private static final int POS_X = 152;
    private static final int POS_Y = 54;
    private static final int BUTTON_WIDTH = 11;
    private static final int BUTTON_HEIGHT = 13;
    private final Tooltip tooltip = setupTooltip();

    public EnergyDumpButton(
        MachineScreen screen
    ) {
        super(screen, POS_X, POS_Y, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_ID);
    }

    private static Tooltip setupTooltip() {
        return Tooltip
            .builder()
            .title("dump.title")
            .blank()
            .line(() -> !Screen.hasShiftDown(), "dump.description")
            .line(Screen::hasShiftDown, "dump.warning", TextFormatting.DARK_RED)
            .blank()
            .shiftClickAction("dump.action");
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        if (screen.isHovered(mX, mY, POS_X, POS_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            screen.renderComponentTooltip(matrix, tooltip.build(), mX, mY);
        }
    }

    @Override
    protected void clickHandler() {
        if (!Screen.hasShiftDown()) return;
        PacketHandler.CHANNEL.sendToServer(new EnergyDumpPacket());
    }

    @Override
    protected int getTextureWidth() {
        return BUTTON_WIDTH;
    }

    @Override
    protected int getTextureHeight() {
        return BUTTON_HEIGHT;
    }
}
