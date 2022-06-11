package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.assembler.PatternHolderMenu;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.almostreliable.lazierae2.core.Constants.Blocks.PATTERN_HOLDER_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class PatternHolderScreen extends GenericScreen<PatternHolderMenu> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL(f("textures/gui/{}.png", PATTERN_HOLDER_ID));
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 158;
    private static final int SLOT_SIZE = 18;

    public PatternHolderScreen(
        PatternHolderMenu menu, Inventory inventory, Component ignoredTitle
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

        // pattern slots
        renderPatternRows(stack, getMenu().entity.getTier().ordinal() + 1);
    }

    private void renderPatternRows(PoseStack stack, int rows) {
        for (var row = 0; row < rows; row++) {
            for (var slot = 0; slot < 9; slot++) {
                var x = 7 + leftPos + slot * SLOT_SIZE;
                var y = 10 + topPos + row * SLOT_SIZE;
                blit(stack, x, y, 7, 75, SLOT_SIZE, SLOT_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
        }
    }
}
