package com.almostreliable.lazierae.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.almostreliable.lazierae.LazierAE2Constants;
import com.almostreliable.lazierae.client.screen.toolbar.AnchoredToolbar;
import com.almostreliable.lazierae.client.screen.toolbar.ToolbarAnchor;
import com.almostreliable.lazierae.client.screen.toolbar.ToolbarHost;
import com.almostreliable.lazierae.client.screen.toolbar.widget.ProgressDisplay;
import com.almostreliable.lazierae.client.screen.toolbar.widget.SideConfigToolbarButton;
import com.almostreliable.lazierae.client.screen.widget.DraggableWidget;
import com.almostreliable.lazierae.client.screen.widget.SideConfigWidget;
import com.almostreliable.lazierae.menu.CrystalInjectorMenu;
import com.almostreliable.lib.client.gui.AlmostContainerScreen;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;
import com.almostreliable.lib.util.Area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

public class CrystalInjectorScreen extends AlmostContainerScreen<CrystalInjectorMenu> implements ToolbarHost {

    private static final ResourceLocation TEXTURE = AlmostWidget.getTexture(LazierAE2Constants.MOD_ID, "gui/injector");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private final AnchoredToolbar topLeftToolbar;
    private final AnchoredToolbar rightTopToolbar;

    public CrystalInjectorScreen(CrystalInjectorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.topLeftToolbar = new AnchoredToolbar(this, ToolbarAnchor.LEFT_BOTTOM);
        this.rightTopToolbar = new AnchoredToolbar(this, ToolbarAnchor.RIGHT_TOP, 2, 0, 4);
    }

    @Override
    protected void init() {
        super.init();

        topLeftToolbar.displayWidget(new ProgressDisplay(this));
        topLeftToolbar.displayWidget(new ProgressDisplay(this));
        addRenderableWidget(topLeftToolbar);

        rightTopToolbar.interactableWidget(new SideConfigToolbarButton());
        rightTopToolbar.displayWidget(new ProgressDisplay(this));
        addRenderableWidget(rightTopToolbar);

        SideConfigWidget sideConfigWidget = new SideConfigWidget(getMenu().getBlockEntity(), Area.of(80, 80, 200, 150));
        DraggableWidget draggableWidget = DraggableWidget.of(this, sideConfigWidget, 4, 2);
        addRenderableWidget(draggableWidget);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        topLeftToolbar.clear();
        rightTopToolbar.clear();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }
}
