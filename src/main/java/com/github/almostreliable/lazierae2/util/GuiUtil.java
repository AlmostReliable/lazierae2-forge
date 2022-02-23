package com.github.almostreliable.lazierae2.util;

import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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

        private final List<Tuple<ITextComponent, Supplier<?>[]>> components;

        private Tooltip() {
            components = new ArrayList<>();
        }

        public static Tooltip builder() {
            return new Tooltip();
        }

        /**
         * Returns the built tooltip as text component list.
         * <p>
         * Replaces placeholders where necessary.
         * <p>
         * If no replacements have been used, this method should be directly called
         * after building the tooltip and statically saved for better performance.
         * <p>
         * {@code
         * %s = String
         * %d = Number
         * %1$s = ordered placeholder
         * }
         *
         * @return the list of tooltip components
         */
        public List<ITextComponent> resolve() {
            List<ITextComponent> list = new ArrayList<>();

            for (Tuple<ITextComponent, Supplier<?>[]> tuple : components) {
                ITextComponent component = tuple.getA();
                Supplier<?>[] suppliers = tuple.getB();
                if (suppliers.length > 0 && component instanceof TranslationTextComponent) {
                    list.add(new TranslationTextComponent(((TranslationTextComponent) component).getKey(),
                        Arrays.stream(suppliers).map(Supplier::get).toArray()
                    ));
                    continue;
                }
                list.add(component);
            }

            return list;
        }

        /**
         * Adds a generic component to the tooltip which is not covered by the builder.
         *
         * @param component    the component to add
         * @param replacements the optional replacements to apply to the component
         * @return the instance of the tooltip builder
         */
        public Tooltip component(ITextComponent component, Supplier<?>... replacements) {
            components.add(new Tuple<>(component, replacements));
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
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the header
         * @return the instance of the tooltip builder
         */
        public Tooltip header(String key, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GOLD), replacements);
        }

        /**
         * Adds a description component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the description
         * @return the instance of the tooltip builder
         */
        public Tooltip description(String key, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE), replacements);
        }

        /**
         * Adds a click action component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the click action
         * @return the instance of the tooltip builder
         */
        public Tooltip clickAction(String key, Supplier<?>... replacements) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", TextFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)), replacements);
        }

        /**
         * Adds a shift click action component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the shift click action
         * @return the instance of the tooltip builder
         */
        public Tooltip shiftClickAction(String key, Supplier<?>... replacements) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.colorize(String.format("%s + %s",
                    InputMappings.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                    TextUtil.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                ), TextFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)), replacements);
        }

        /**
         * Adds a hotkey action component to the tooltip.
         *
         * @param hotkey       the hotkey from the {@link InputMappings}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyAction(String hotkey, String key, Supplier<?>... replacements) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.colorize(InputMappings.getKey(hotkey).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)), replacements);
        }

        /**
         * Adds a hotkey hold action component to the tooltip.
         *
         * @param hotkey       the hotkey from the {@link InputMappings}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey hold action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyHoldAction(String hotkey, String key, Supplier<?>... replacements) {
            return component(TextUtil
                .colorize("> ", TextFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", TextFormatting.GRAY))
                .append(" ")
                .append(TextUtil.colorize(InputMappings.getKey(hotkey).getDisplayName().getString(),
                    TextFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)), replacements);
        }
    }
}
