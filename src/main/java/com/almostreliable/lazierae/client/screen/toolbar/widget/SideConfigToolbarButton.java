package com.almostreliable.lazierae.client.screen.toolbar.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lib.client.gui.AlmostGuiEventListener;
import com.almostreliable.lib.client.gui.WidgetData;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public class SideConfigToolbarButton implements AlmostWidget<WidgetData>, AlmostGuiEventListener<WidgetData> {

    private static final ResourceLocation TEXTURE = AlmostWidget.getTexture(LazierAE2Constants.MOD_ID, "gui/right_top");

    private final WidgetData data;

    public SideConfigToolbarButton() {
        this.data = WidgetData.of(26, 20);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        AlmostWidget.super.render(poseStack, mouseX, mouseY, delta);

        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, data.getX(), data.getY(), 0, 0, data.getWidth(), data.getHeight(), data.getWidth(), data.getHeight());

        String label = "I/O";
        Font font = Minecraft.getInstance().font;
        int labelWidth = font.width(label);

        GuiComponent.drawString(
            poseStack,
            font,
            label,
            data.getX() + (data.getWidth() - labelWidth) / 2 - 2,
            data.getY() + (data.getHeight() - font.lineHeight) / 2,
            0xFFFF_FFFF
        );
    }

    @Override
    public WidgetData getData() {
        return data;
    }
}
