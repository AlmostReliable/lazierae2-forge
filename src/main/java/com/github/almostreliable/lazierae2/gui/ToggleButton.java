package com.github.almostreliable.lazierae2.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

import java.util.function.BooleanSupplier;

public abstract class ToggleButton extends GenericButton {

    final BooleanSupplier pressed;

    ToggleButton(
        MachineScreen screen, int pX, int pY, int width, int height, String textureID, BooleanSupplier pressed
    ) {
        super(screen, pX, pY, width, height, textureID);
        this.pressed = pressed;
    }

    @Override
    public void renderButton(
        MatrixStack matrix, int mX, int mY, float partial
    ) {
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrix, x, y, 0, pressed.getAsBoolean() ? height : 0, width, height, width, height * 2);
    }

    @Override
    protected int getTextureWidth() {
        return width;
    }

    @Override
    protected int getTextureHeight() {
        return height * 2;
    }
}
