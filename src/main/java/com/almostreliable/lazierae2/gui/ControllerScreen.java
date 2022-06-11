package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.assembler.controller.ControllerMenu;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.almostreliable.lazierae2.core.Constants.Blocks.CONTROLLER_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ControllerScreen extends GenericScreen<ControllerMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL(f("textures/gui/{}.png", CONTROLLER_ID));
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 158;
    // private static final int SLOT_SIZE = 18;

    public ControllerScreen(
        ControllerMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mX, int mY) {
        drawCenteredString(stack, font, title, TEXTURE_WIDTH / 2, -12, 0xFFFF_FFFF);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
