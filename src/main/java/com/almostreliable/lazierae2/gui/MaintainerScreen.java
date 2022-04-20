package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.gui.widgets.MaintainerControl;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MaintainerScreen extends GenericScreen<MaintainerMenu> {

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 211;
    private static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/maintainer.png");
    private final MaintainerControl[] maintainerControl;

    @SuppressWarnings({"AssignmentToSuperclassField", "ThisEscapedInObjectConstruction"})
    public MaintainerScreen(
        MaintainerMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        maintainerControl = MaintainerControl.create(this, menu.getRequestSlots());
    }

    public void updateCountBox(int slot, long count) {
        maintainerControl[slot].updateCountBox(count);
    }

    public void updateBatchBox(int slot, long batch) {
        maintainerControl[slot].updateBatchBox(batch);
    }

    @Override
    protected void init() {
        super.init();
        for (var control : maintainerControl) {
            addRenderables(control.createWidgets());
        }
    }

    @Override
    protected void renderLabels(PoseStack stack, int mX, int mY) {
        drawCenteredString(stack, font, title, TEXTURE_WIDTH / 2, -12, 16_777_215);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
