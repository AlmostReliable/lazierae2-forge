package com.github.almostreliable.lazierae2.gui;

import com.github.almostreliable.lazierae2.machine.MachineBlock;
import com.github.almostreliable.lazierae2.machine.MachineContainer;
import com.github.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.github.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collection;

public class MachineScreen extends ContainerScreen<MachineContainer> {

    private static final int TEXTURE_WIDTH = 178;
    private static final int TEXTURE_HEIGHT = 154;
    private static final int PROGRESS_WIDTH = 40;
    private static final int PROGRESS_HEIGHT = 27;
    private static final int ENERGY_WIDTH = 2;
    private static final int ENERGY_HEIGHT = 58;
    private static final int SLOT_SIZE = 18;
    private static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/machine.png");
    private final ResourceLocation progressTexture;
    private final Collection<Widget> renderables = new ArrayList<>();
    private final Tooltip progressTooltip;
    // private final Tooltip energyTooltip;

    public MachineScreen(
        MachineContainer container, PlayerInventory inventory, ITextComponent ignoredTitle
    ) {
        super(container, inventory, container.tile.getDisplayName());
        progressTexture = TextUtil.getRL("textures/gui/progress/" + container.tile.getMachineType() + ".png");
        progressTooltip = setupProgressTooltip();
        // energyTooltip = setupEnergyTooltip();
    }

    @Override
    protected void init() {
        super.init();
        addRenderable(new AutoExtractButton(this, menu.tile::isAutoExtract));
        addRenderable(new EnergyDumpButton(this));
        addRenderables(IOControl.setup(this, 7, 7));
    }

    @Override
    public void render(MatrixStack matrix, int mX, int mY, float partial) {
        renderBackground(matrix);
        super.render(matrix, mX, mY, partial);
        renderTooltip(matrix, mX, mY);
    }

    @Override
    protected void renderTooltip(MatrixStack matrix, int mX, int mY) {
        super.renderTooltip(matrix, mX, mY);

        // progress bar
        if (isHovered(mX, mY, 78, 23, PROGRESS_WIDTH / 2, PROGRESS_HEIGHT)) {
            renderComponentTooltip(matrix, progressTooltip.resolve(), mX, mY);
        }

        for (Widget widget : renderables) {
            if (widget.isHovered() && widget.visible) {
                widget.renderToolTip(matrix, mX, mY);
            }
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrix, int mX, int mY) {
        drawCenteredString(matrix, font, title, (TEXTURE_WIDTH - ENERGY_WIDTH) / 2, -12, 16_777_215);

        // TODO: remove
        drawString(matrix, font, "Energy: " + menu.getEnergyStored(), 10, 10, 0xff_ffff);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void renderBg(MatrixStack matrix, float partial, int mX, int mY) {
        // background texture
        if (minecraft == null) return;
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrix,
            leftPos,
            topPos,
            0,
            0,
            TEXTURE_WIDTH - ENERGY_WIDTH,
            TEXTURE_HEIGHT,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );

        // upper and lower input slots for triple input machines
        if (menu.tile.getMachineType().getInputSlots() == 3) {
            blit(matrix, leftPos + 43, topPos + 7, 43, 28, SLOT_SIZE, SLOT_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            blit(matrix, leftPos + 43, topPos + 49, 43, 28, SLOT_SIZE, SLOT_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        // energy bar
        int capacity = menu.getEnergyCapacity();
        int energy = Math.min(menu.getEnergyStored(), capacity);
        int barHeight = energy > 0 ? energy * ENERGY_HEIGHT / capacity : 0;
        blit(matrix,
            leftPos + 166,
            topPos + 66 - barHeight,
            176,
            0,
            ENERGY_WIDTH,
            barHeight,
            TEXTURE_WIDTH,
            TEXTURE_HEIGHT
        );

        // progress bar
        minecraft.getTextureManager().bind(progressTexture);
        int progress = menu.tile.getProgress();
        int processTime = menu.tile.getProcessTime();
        int barWidth = processTime > 0 ? progress * (PROGRESS_WIDTH / 2) / processTime : 0;
        blit(matrix,
            leftPos + 78,
            topPos + 24,
            0,
            0,
            PROGRESS_WIDTH / 2,
            PROGRESS_HEIGHT,
            PROGRESS_WIDTH,
            PROGRESS_HEIGHT
        );
        blit(matrix,
            leftPos + 78,
            topPos + 24,
            PROGRESS_WIDTH / 2f,
            0,
            barWidth,
            PROGRESS_HEIGHT,
            PROGRESS_WIDTH,
            PROGRESS_HEIGHT
        );
    }

    private Tooltip setupProgressTooltip() {
        return Tooltip
            .builder()
            .header("progress.header")
            .blankLine()
            .conditional(progress -> progress
                .condition(() -> (menu.tile.getProgress() > 0 && menu.tile.getProcessTime() > 0) ||
                    menu.tile.getBlockState().getValue(MachineBlock.ACTIVE).equals(true))
                .then(Tooltip
                    .builder()
                    .keyValue("progress.progress", menu.tile::getProgress, menu.tile::getProcessTime)
                    .conditional(extendedInfo -> extendedInfo
                        .condition(Screen::hasShiftDown)
                        .then(Tooltip
                            .builder()
                            .keyValueIf(menu::hasUpgrades, "progress.recipe_time", menu.tile::getRecipeTime)
                            .keyValueIf(menu::hasUpgrades, "progress.time_multiplier", this::getProcessTimeMultiplier)
                            .blankLineIf(menu::hasUpgrades)
                            .keyValue("progress.energy",
                                () -> TextUtil.formatEnergy(menu.tile.getEnergyCost(), 1, 2, false, true)
                            )
                            .keyValueIf(menu::hasUpgrades,
                                "progress.recipe_energy",
                                () -> TextUtil.formatEnergy(menu.tile.getRecipeEnergy(), 1, 2, false, true)
                            )
                            .keyValueIf(menu::hasUpgrades, "progress.energy_multiplier", this::getEnergyCostMultiplier))
                        .otherwise(Tooltip
                            .builder()
                            .blankLine()
                            .hotkeyHoldAction("key.keyboard.left.shift", "extended_info"))))
                .otherwise(Tooltip.builder().description("progress.none")));
    }

    private String getMultiplier(int currentVal, int recipeVal) {
        return TextUtil.formatNumber((double) currentVal / recipeVal, 1, 3);
    }

    private boolean isHovered(int mX, int mY, int x, int y, int width, int height) {
        return mX >= x + leftPos && mX <= x + width + leftPos && mY >= y + topPos && mY <= y + height + topPos;
    }

    private void addRenderable(Widget widget) {
        addButton(widget);
        renderables.add(widget);
    }

    private void addRenderables(Widget... widgets) {
        for (Widget widget : widgets) {
            addRenderable(widget);
        }
    }

    private String getProcessTimeMultiplier() {
        int processTime = menu.tile.getProcessTime();
        int recipeTime = menu.tile.getRecipeTime();
        return getMultiplier(processTime, recipeTime);
    }

    private String getEnergyCostMultiplier() {
        int energyCost = menu.tile.getEnergyCost();
        int recipeEnergy = menu.tile.getRecipeEnergy();
        return getMultiplier(energyCost, recipeEnergy);
    }
}
