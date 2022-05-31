package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.requester.RequesterMenu;
import com.almostreliable.lazierae2.gui.control.RequestControl;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.almostreliable.lazierae2.core.Constants.Blocks.REQUESTER_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class RequesterScreen extends GenericScreen<RequesterMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL(f("textures/gui/{}.png", REQUESTER_ID));
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 211;
    public final RequestControl requestControl;

    @SuppressWarnings({"AssignmentToSuperclassField", "ThisEscapedInObjectConstruction"})
    public RequesterScreen(
        RequesterMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        imageWidth = TEXTURE_WIDTH;
        imageHeight = TEXTURE_HEIGHT;
        requestControl = new RequestControl(this, menu.entity.craftRequests.getSlots());
    }

    @Override
    protected void init() {
        super.init();
        addRenderables(requestControl.init());
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (InputConstants.getKey("key.keyboard.tab").getValue() == keyCode) {
            // if tab is pressed, let the widget handle it
            return getFocused() != null && getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
