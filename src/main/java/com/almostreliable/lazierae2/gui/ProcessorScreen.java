package com.almostreliable.lazierae2.gui;

import com.almostreliable.lazierae2.content.GenericBlock;
import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.gui.control.IOControl;
import com.almostreliable.lazierae2.gui.widgets.AutoExtractButton;
import com.almostreliable.lazierae2.gui.widgets.EnergyDumpButton;
import com.almostreliable.lazierae2.inventory.UpgradeSlot;
import com.almostreliable.lazierae2.util.GuiUtil.Tooltip;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ProcessorScreen extends GenericScreen<ProcessorMenu> {

    public static final int TEXTURE_WIDTH = 178;
    public static final int TEXTURE_HEIGHT = 154;
    public static final int PROGRESS_WIDTH = 40;
    public static final int PROGRESS_HEIGHT = 27;
    public static final ResourceLocation TEXTURE = TextUtil.getRL("textures/gui/processor.png");
    public static final int SLOT_SIZE = 18;
    public static final int ENERGY_WIDTH = 2;
    private static final int ENERGY_HEIGHT = 58;
    private final ResourceLocation progressTexture;
    private final Tooltip progressTooltip;
    private final Tooltip energyTooltip;
    private final Tooltip upgradeTooltip;

    public ProcessorScreen(
        ProcessorMenu menu, Inventory inventory, Component ignoredTitle
    ) {
        super(menu, inventory);
        progressTexture = TextUtil.getRL(f("textures/gui/progress/{}.png", menu.entity.getProcessorType()));
        progressTooltip = setupProgressTooltip();
        energyTooltip = setupEnergyTooltip();
        upgradeTooltip = setupUpgradeTooltip();
    }

    @Override
    protected void init() {
        super.init();
        addRenderable(new AutoExtractButton(this, menu.entity::isAutoExtracting));
        addRenderable(new EnergyDumpButton(this));
        addRenderables(IOControl.setup(this, 7, 7));
    }

    @Override
    protected void renderLabels(PoseStack stack, int mX, int mY) {
        drawCenteredString(stack, font, title, (TEXTURE_WIDTH - ENERGY_WIDTH) / 2, -12, 16_777_215);
    }

    @Override
    protected void renderBg(PoseStack stack, float partial, int mX, int mY) {
        // background texture
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(stack, leftPos, topPos, 0, 0, TEXTURE_WIDTH - ENERGY_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // upper and lower input slots for triple input processors
        if (menu.entity.getProcessorType().getInputSlots() == 3) {
            blit(stack, leftPos + 43, topPos + 7, 43, 28, SLOT_SIZE, SLOT_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            blit(stack, leftPos + 43, topPos + 49, 43, 28, SLOT_SIZE, SLOT_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        // energy bar
        var capacity = menu.getEnergyCapacity();
        var energy = Math.min(menu.getEnergyStored(), capacity);
        var barHeight = energy > 0 ? energy * ENERGY_HEIGHT / capacity : 0;
        blit(
            stack,
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
        RenderSystem.setShaderTexture(0, progressTexture);
        var progress = menu.entity.getProgress();
        var processTime = menu.entity.getProcessTime();
        var barWidth = processTime > 0 ? progress * (PROGRESS_WIDTH / 2) / processTime : 0;
        blit(
            stack,
            leftPos + 78,
            topPos + 24,
            0,
            0,
            PROGRESS_WIDTH / 2,
            PROGRESS_HEIGHT,
            PROGRESS_WIDTH,
            PROGRESS_HEIGHT
        );
        blit(
            stack,
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

    @Override
    protected void renderTooltip(PoseStack stack, int mX, int mY) {
        // progress bar
        if (isHovered(mX, mY, 78, 23, PROGRESS_WIDTH / 2, PROGRESS_HEIGHT)) {
            renderComponentTooltip(stack, progressTooltip.build(), mX, mY);
            return;
        }
        // energy bar
        if (isHovered(mX, mY, 165, 7, ENERGY_WIDTH + 2, ENERGY_HEIGHT + 2)) {
            renderComponentTooltip(stack, energyTooltip.build(), mX, mY);
            return;
        }
        // upgrade slot
        if (hoveredSlot instanceof UpgradeSlot) {
            renderComponentTooltip(stack, upgradeTooltip.build(), mX, mY);
            return;
        }

        super.renderTooltip(stack, mX, mY);
    }

    private Tooltip setupProgressTooltip() {
        return Tooltip
            .builder()
            .title("progress.title")
            .blank()
            .conditional(progress -> progress
                .condition(() -> (menu.entity.getProgress() > 0 && menu.entity.getProcessTime() > 0) ||
                    menu.entity.getBlockState().getValue(GenericBlock.ACTIVE).equals(true))
                .then(Tooltip
                    .builder()
                    .keyValue("progress.progress", menu.entity::getProgress, menu.entity::getProcessTime)
                    .conditional(extendedInfo -> extendedInfo
                        .condition(Screen::hasShiftDown)
                        .then(Tooltip
                            .builder()
                            .keyValue(menu::hasUpgrades, "progress.recipe_time", menu.entity::getRecipeTime)
                            .keyValue(menu::hasUpgrades, "progress.time_multiplier", this::getProcessTimeMultiplier)
                            .blank(menu::hasUpgrades)
                            .keyValue(
                                "progress.energy",
                                () -> TextUtil.formatEnergy(menu.entity.getEnergyCost(), 1, 2, false, true)
                            )
                            .keyValue(
                                menu::hasUpgrades,
                                "progress.recipe_energy",
                                () -> TextUtil.formatEnergy(menu.entity.getRecipeEnergy(), 1, 2, false, true)
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
            .keyValue(
                "energy.capacity",
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
                        () -> menu.entity.getProcessorType().getUpgradeSlots()
                    )
                    .keyValue("upgrade.additional", this::getAdditionalUpgradeEnergy))
                .otherwise(Tooltip
                    .builder()
                    .line("upgrade.none", ChatFormatting.YELLOW)
                    .blank()
                    .line("upgrade.description")));
    }

    private String getMultiplier(int currentVal, int recipeVal) {
        return TextUtil.formatNumber((double) currentVal / recipeVal, 1, 3);
    }

    private String getProcessTimeMultiplier() {
        var processTime = menu.entity.getProcessTime();
        var recipeTime = menu.entity.getRecipeTime();
        return getMultiplier(processTime, recipeTime);
    }

    private String getEnergyCostMultiplier() {
        var energyCost = menu.entity.getEnergyCost();
        var recipeEnergy = menu.entity.getRecipeEnergy();
        return getMultiplier(energyCost, recipeEnergy);
    }

    private String getAdditionalUpgradeEnergy() {
        assert hoveredSlot != null;
        var additional = menu.entity.getProcessorType().getEnergyBufferAdd() * menu.getUpgradeCount();
        return TextUtil.formatEnergy(additional, 1, 2, Screen.hasShiftDown(), true);
    }
}
