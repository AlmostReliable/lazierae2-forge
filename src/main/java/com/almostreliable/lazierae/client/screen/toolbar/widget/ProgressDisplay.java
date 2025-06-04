package com.almostreliable.lazierae.client.screen.toolbar.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lazierae.client.screen.CrystalInjectorScreen;
import com.almostreliable.lib.client.gui.WidgetData;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public class ProgressDisplay implements AlmostWidget<WidgetData> {

    private static final ResourceLocation TEXTURE = AlmostWidget.getTexture(LazierAE2Constants.MOD_ID, "gui/progress_display");

    private final WidgetData data;
    private final CrystalInjectorScreen screen;

    public ProgressDisplay(CrystalInjectorScreen screen) {
        this.data = WidgetData.of(20, 20);
        this.screen = screen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        AlmostWidget.super.render(poseStack, mouseX, mouseY, delta);

        int x = data.getX();
        int y = data.getY();
        int width = data.getWidth();
        int height = data.getHeight();

        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, x, y, 0, 0, width, height, width, height);

        int progress = screen.getMenu().getProgress();
        int maxProgress = screen.getMenu().getMaxProgress();
        float percentage = (float) Math.round((float) progress / maxProgress * 10_000) / 100;
        GuiComponent.drawCenteredString(
            poseStack,
            Minecraft.getInstance().font,
            percentage + " %",
            x + width / 2,
            y + height / 2 - 4,
            0xFFFF_FFFF
        );
    }

    @Override
    public WidgetData getData() {
        return data;
    }
}
