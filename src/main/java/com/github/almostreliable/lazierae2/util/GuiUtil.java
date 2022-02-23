package com.github.almostreliable.lazierae2.util;

import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public final class GuiUtil {

    private GuiUtil() {}

    /**
     * Draws a given text at the given position with the given color.
     * <p>
     * This method handles the translation and scaling of the text in order
     * to maintain the same position after the scaling.
     *
     * @param matrix the matrix stack for the rendering
     * @param x      the x position
     * @param y      the y position
     * @param scale  the scale of the text
     * @param text   the text to draw
     * @param color  the color of the text as decimal
     */
    public static void renderText(MatrixStack matrix, int x, int y, float scale, String text, int color) {
        matrix.pushPose();
        matrix.translate(x, y, 0);
        matrix.scale(scale, scale, 1);
        Minecraft.getInstance().font.draw(matrix, text, 0, 0, color);
        matrix.popPose();
    }

    public static final class Tooltip {

        private final List<ITextComponent> components;

        private Tooltip() {
            components = new ArrayList<>();
        }

        public static Tooltip builder() {
            return new Tooltip();
        }

        /**
         * Returns the built tooltip as list.
         *
         * @return the list of tooltip components
         */
        public List<ITextComponent> build() {
            return components;
        }

        /**
         * Adds a generic component to the tooltip which is not covered by the builder.
         *
         * @param component the component to add
         * @return the instance of the tooltip builder
         */
        public Tooltip component(ITextComponent component) {
            components.add(component);
            return this;
        }

        /**
         * Adds a blank line to the tooltip.
         * <p>
         * Instead of adding an empty {@link StringTextComponent}, this method adds a line with a single space
         * to enforce the blank line because auto line breaks caused issues in the past.
         *
         * @return the instance of the tooltip builder
         */
        public Tooltip blankLine() {
            return component(new StringTextComponent(" "));
        }

        /**
         * Adds a header component to the tooltip.
         * <p>
         * It has golden text color. It's language key is {@code "mod-id.tooltip.key"}.
         *
         * @param key the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip header(String key) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GOLD));
        }

        /**
         * Adds a description component to the tooltip.
         *
         * @param key the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip description(String key) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE));
        }

        /**
         * Adds a click action component to the tooltip.
         *
         * @param key the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip clickAction(String key) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", TextFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a shift click action component to the tooltip.
         *
         * @param key the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip shiftClickAction(String key) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.colorize(String.format("%s + %s",
                    InputMappings.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                    TextUtil.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                ), TextFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a hotkey action component to the tooltip.
         *
         * @param hotkey the hotkey from the {@link InputMappings}
         * @param key    the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyAction(String hotkey, String key) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.colorize(InputMappings.getKey(hotkey).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }

        /**
         * Adds a hotkey hold action component to the tooltip.
         *
         * @param hotkey the hotkey from the {@link InputMappings}
         * @param key    the key for the translation
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyHoldAction(String hotkey, String key) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", TextFormatting.GRAY))
                .append(" ")
                .append(TextUtil.colorize(InputMappings.getKey(hotkey).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)));
        }
    }
}
