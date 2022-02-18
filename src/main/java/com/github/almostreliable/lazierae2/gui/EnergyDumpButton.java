package com.github.almostreliable.lazierae2.gui;

import com.github.almostreliable.lazierae2.network.EnergyDumpPacket;
import com.github.almostreliable.lazierae2.network.PacketHandler;
import com.mojang.blaze3d.matrix.MatrixStack;

public class EnergyDumpButton extends GenericButton {

    private static final String TEXTURE_ID = "dump";
    private static final int POS_X = 153;
    private static final int POS_Y = 54;
    private static final int BUTTON_WIDTH = 11;
    private static final int BUTTON_HEIGHT = 13;

    EnergyDumpButton(
        MachineScreen screen
    ) {
        super(screen, POS_X, POS_Y, BUTTON_WIDTH, BUTTON_HEIGHT, TEXTURE_ID);
    }

    @Override
    public void renderToolTip(MatrixStack matrix, int mX, int mY) {
        // TODO
    }

    @Override
    protected void clickHandler() {
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
