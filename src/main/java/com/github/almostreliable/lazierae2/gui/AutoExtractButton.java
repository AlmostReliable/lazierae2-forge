package com.github.almostreliable.lazierae2.gui;

import com.github.almostreliable.lazierae2.network.AutoExtractPacket;
import com.github.almostreliable.lazierae2.network.PacketHandler;
import com.github.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.BooleanSupplier;

public class AutoExtractButton extends Button {

    private static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/buttons/auto_extract.png");
    private static final int POS_X = 7;
    private static final int POS_Y = 28;
    private static final int BUTTON_SIZE = 18;
    private final BooleanSupplier pressed;

    AutoExtractButton(
        MachineScreen screen, BooleanSupplier pressed
    ) {
        super(
            screen.getGuiLeft() + POS_X,
            screen.getGuiTop() + POS_Y,
            BUTTON_SIZE,
            BUTTON_SIZE,
            StringTextComponent.EMPTY,
            button -> ((AutoExtractButton) button).clickHandler()
        );
        this.pressed = pressed;
    }

    @Override
    public void renderButton(
        MatrixStack matrix, int mX, int mY, float partial
    ) {
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(
            matrix,
            x,
            y,
            0,
            pressed.getAsBoolean() ? BUTTON_SIZE : 0,
            BUTTON_SIZE,
            BUTTON_SIZE,
            BUTTON_SIZE,
            BUTTON_SIZE * 2
        );
    }

    private void clickHandler() {
        PacketHandler.CHANNEL.sendToServer(new AutoExtractPacket(!pressed.getAsBoolean()));
    }
}
