package com.almostreliable.lazierae2.gui.widgets;

import com.almostreliable.lazierae2.gui.GenericScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.BooleanSupplier;

public abstract class ToggleButton extends GenericButton {

    protected final BooleanSupplier pressed;

    protected ToggleButton(
        GenericScreen<?> screen, int pX, int pY, int width, int height, String textureID, BooleanSupplier pressed
    ) {
        super(screen, pX, pY, width, height, textureID);
        this.pressed = pressed;
    }

    @Override
    public void renderButton(
        PoseStack stack, int mX, int mY, float partial
    ) {
        RenderSystem.setShaderTexture(0, texture);
        blit(stack, x, y, 0, pressed.getAsBoolean() ? height : 0, width, height, width, height * 2);
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
