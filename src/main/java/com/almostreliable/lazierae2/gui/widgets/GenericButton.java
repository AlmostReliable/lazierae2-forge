package com.almostreliable.lazierae2.gui.widgets;

import com.almostreliable.lazierae2.gui.MachineScreen;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class GenericButton extends Button {

    final ResourceLocation texture;
    MachineScreen screen;

    GenericButton(
        MachineScreen screen, int pX, int pY, int width, int height, String textureID
    ) {
        super(
            screen.getGuiLeft() + pX,
            screen.getGuiTop() + pY,
            width,
            height,
            TextComponent.EMPTY,
            button -> ((GenericButton) button).clickHandler()
        );
        this.screen = screen;
        texture = TextUtil.getRL(f("textures/gui/buttons/{}.png", textureID));
    }

    @Override
    public void renderButton(
        PoseStack stack, int mX, int mY, float partial
    ) {
        RenderSystem.setShaderTexture(0, texture);
        blit(stack, x, y, 0, 0, width, height, getTextureWidth(), getTextureHeight());
    }

    protected abstract void clickHandler();

    protected abstract int getTextureWidth();

    protected abstract int getTextureHeight();
}
