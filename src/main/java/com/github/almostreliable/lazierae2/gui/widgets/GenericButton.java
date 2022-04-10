package com.github.almostreliable.lazierae2.gui.widgets;

import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public abstract class GenericButton extends Button {

    final ResourceLocation texture;

    GenericButton(
        MachineScreen screen, int pX, int pY, int width, int height, String textureID
    ) {
        super(
            screen.getGuiLeft() + pX,
            screen.getGuiTop() + pY,
            width,
            height,
            StringTextComponent.EMPTY,
            button -> ((GenericButton) button).clickHandler()
        );
        texture = TextUtil.getRL("textures/gui/buttons/" + textureID + ".png");
    }

    @Override
    public void renderButton(
        MatrixStack matrix, int mX, int mY, float partial
    ) {
        Minecraft.getInstance().getTextureManager().bind(texture);
        blit(matrix, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
    }

    protected abstract void clickHandler();

    protected abstract int getTextureWidth();

    protected abstract int getTextureHeight();
}
