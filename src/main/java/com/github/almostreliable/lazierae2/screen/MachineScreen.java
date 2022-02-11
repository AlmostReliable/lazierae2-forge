package com.github.almostreliable.lazierae2.screen;

import com.github.almostreliable.lazierae2.container.MachineContainer;
import com.github.almostreliable.lazierae2.tile.MachineTile;
import com.github.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class MachineScreen extends ContainerScreen<MachineContainer> {

    private static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/machine.png");
    private final MachineTile tile;

    public MachineScreen(
        MachineContainer container, PlayerInventory inventory, ITextComponent ignoredTitle
    ) {
        super(container, inventory, StringTextComponent.EMPTY);
        tile = container.getTile();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(MatrixStack matrix, float partial, int pX, int pY) {
        // background texture
        if (minecraft == null) return;
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrix, leftPos, topPos, 0, 0, 176, 154, 194, 154);

        // input slots for triple inputs
        if (tile.getInputSlots() == 3) {
            blit(matrix, leftPos + 43, topPos + 7, 43, 28, 18, 18, 194, 154);
            blit(matrix, leftPos + 43, topPos + 49, 43, 28, 18, 18, 194, 154);
        }

        // bars
        int energy = menu.getEnergyStored();
        int capacity = menu.getEnergyCapacity();
        int barHeight = energy > 0 ? energy * 58 / capacity : 0;
        blit(matrix, leftPos + 166, topPos + 8, 176, 18, 2, 58 - barHeight, 194, 154);

        // progress bar
        minecraft.getTextureManager().bind(TextUtil.getRL("textures/gui/progress/" + tile.getId() + ".png"));
        int progress = menu.getTile().getProgress();
        int processTime = menu.getTile().getProcessTime();
        int barWidth = progress > 0 ? progress * 20 / processTime : 0;
        blit(matrix, leftPos + 78, topPos + 24, 0, 0, barWidth, 27, 40, 27);
    }
}
