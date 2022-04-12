package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.gui.widgets.AutoExtractButton;
import com.almostreliable.lazierae2.gui.widgets.EnergyDumpButton;
import com.almostreliable.lazierae2.gui.widgets.IOControl;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.machine.MachineBlock;
import com.almostreliable.lazierae2.machine.MachineContainer;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collection;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class MachineScreen extends ContainerScreen<MachineContainer> {

    public static final int TEXTURE_WIDTH = 178;
    public static final int TEXTURE_HEIGHT = 154;
    public static final int PROGRESS_WIDTH = 40;
    public static final int PROGRESS_HEIGHT = 27;
    public static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/machine.png");
    public static final int SLOT_SIZE = 18;
    public static final int ENERGY_WIDTH = 2;
    private static final int ENERGY_HEIGHT = 58;
    private final ResourceLocation progressTexture;
    private final Collection<Widget> renderables = new ArrayList<>();
    private final Tooltip progressTooltip;
    private final Tooltip energyTooltip;
    private final Tooltip upgradeTooltip;

    public MachineScreen(
        MachineContainer container, PlayerInventory inventory, ITextComponent ignoredTitle
    ) {
        super(container, inventory, container.tile.getDisplayName());
        progressTexture = TextUtil.getRL(f("textures/gui/progress/{}.png", container.tile.getMachineType()));
        progressTooltip = setupProgressTooltip();
        energyTooltip = setupEnergyTooltip();
        upgradeTooltip = setupUpgradeTooltip();
    }

    public boolean isHovered(int mX, int mY, int x, int y, int width, int height) {
        return mX >= x + leftPos && mX < x + width + leftPos && mY >= y + topPos && mY < y + height + topPos;
    }

    @Override
    protected void init() {
        super.init();
        addRenderable(new AutoExtractButton(this, menu.tile::isAutoExtracting));
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
        // progress bar
        if (isHovered(mX, mY, 78, 23, PROGRESS_WIDTH / 2, PROGRESS_HEIGHT)) {
            renderComponentTooltip(matrix, progressTooltip.build(), mX, mY);
            return;
        }
        // energy bar
        if (isHovered(mX, mY, 165, 7, ENERGY_WIDTH + 2, ENERGY_HEIGHT + 2)) {
            renderComponentTooltip(matrix, energyTooltip.build(), mX, mY);
            return;
        }
        // upgrade slot
        if (hoveredSlot instanceof UpgradeSlot) {
            renderComponentTooltip(matrix, upgradeTooltip.build(), mX, mY);
            return;
        }

        super.renderTooltip(matrix, mX, mY);

        // widget tooltips
        for (Widget widget : renderables) {
            if (widget.isHovered() && widget.visible) {
                widget.renderToolTip(matrix, mX, mY);
            }
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrix, int mX, int mY) {
        drawCenteredString(matrix, font, title, (TEXTURE_WIDTH - ENERGY_WIDTH) / 2, -12, 16_777_215);
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
            .title("progress.title")
            .blank()
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
                            .keyValue(menu::hasUpgrades, "progress.recipe_time", menu.tile::getRecipeTime)
                            .keyValue(menu::hasUpgrades, "progress.time_multiplier", this::getProcessTimeMultiplier)
                            .blank(menu::hasUpgrades)
                            .keyValue("progress.energy",
                                () -> TextUtil.formatEnergy(menu.tile.getEnergyCost(), 1, 2, false, true)
                            )
                            .keyValue(menu::hasUpgrades,
                                "progress.recipe_energy",
                                () -> TextUtil.formatEnergy(menu.tile.getRecipeEnergy(), 1, 2, false, true)
                            )
                            .keyValue(menu::hasUpgrades, "progress.energy_multiplier", this::getEnergyCostMultiplier))
                        .otherwise(Tooltip
                            .builder()
                            .blank()
                            .hotkeyHoldAction("key.keyboard.left.shift", "extended_info"))))
                .otherwise(Tooltip.builder().line("progress.none")));
    }

    private Tooltip setupEnergyTooltip() {
        return Tooltip
            .builder()
            .title("energy.title")
            .blank()
            .keyValue("energy.current",
                () -> TextUtil.formatEnergy(menu.getEnergyStored(), 1, 3, Screen.hasShiftDown(), true)
            )
            .keyValue("energy.capacity",
                () -> TextUtil.formatEnergy(menu.getEnergyCapacity(), 1, 2, Screen.hasShiftDown(), true)
            )
            .blank(() -> !Screen.hasShiftDown())
            .hotkeyHoldAction(() -> !Screen.hasShiftDown(), "key.keyboard.left.shift", "extended_numbers");
    }

    private Tooltip setupUpgradeTooltip() {
        return Tooltip
            .builder()
            .title("upgrade.title")
            .blank()
            .conditional(tooltip -> tooltip
                .condition(menu::hasUpgrades)
                .then(Tooltip
                    .builder()
                    .keyValue("upgrade.current",
                        menu::getUpgradeCount,
                        () -> menu.tile.getMachineType().getUpgradeSlots()
                    )
                    .keyValue("upgrade.additional", this::getAdditionalUpgradeEnergy))
                .otherwise(Tooltip
                    .builder()
                    .line("upgrade.none", TextFormatting.YELLOW)
                    .blank()
                    .line("upgrade.description")));
    }

    private String getMultiplier(int currentVal, int recipeVal) {
        return TextUtil.formatNumber((double) currentVal / recipeVal, 1, 3);
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

    private String getAdditionalUpgradeEnergy() {
        assert hoveredSlot != null;
        int additional = menu.tile.getMachineType().getEnergyBufferAdd() * menu.getUpgradeCount();
        return TextUtil.formatEnergy(additional, 1, 2, Screen.hasShiftDown(), true);
    }
}
