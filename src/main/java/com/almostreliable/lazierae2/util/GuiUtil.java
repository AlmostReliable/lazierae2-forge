package com.almostreliable.lazierae2.util;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class GuiUtil {

    private GuiUtil() {}

    public static int fillColorAlpha(int color) {
        return 0xFF << 3 * 8 | color;
    }

    /**
     * Draws a given text at the given position with the given color.
     * <p>
     * This method handles the translation and scaling of the text in order
     * to maintain the same position after the scaling.
     * <p>
     * The {@link ANCHOR} can be used to specify the alignment of the text.
     *
     * @param stack  the pose stack for the rendering
     * @param text   the text to draw
     * @param anchor the anchor point of the text
     * @param x      the x position
     * @param y      the y position
     * @param scale  the scale of the text
     * @param color  the color of the text as decimal
     */
    public static void renderText(
        PoseStack stack, String text, ANCHOR anchor, int x, int y, float scale, int color
    ) {
        stack.pushPose();
        stack.translate(x, y, 0);
        stack.scale(scale, scale, 1);

        var xOffset = 0;
        var yOffset = 0;
        var font = Minecraft.getInstance().font;
        var width = font.width(text);
        var height = font.lineHeight;
        switch (anchor) {
            case TOP_LEFT:
                // do nothing
                break;
            case TOP_RIGHT:
                xOffset -= width;
                break;
            case BOTTOM_LEFT:
                yOffset -= height;
                break;
            case BOTTOM_RIGHT:
                xOffset -= width;
                yOffset -= height;
                break;
        }

        font.draw(stack, text, xOffset, yOffset, color);
        stack.popPose();
    }

    public enum ANCHOR {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    @SuppressWarnings({"java:S2160", "UnusedReturnValue", "unused"})
    public static final class Tooltip {

        private final List<TooltipComponent> components;

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
         * If no replacements or conditions have been used, this method should be directly called
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
        public List<Component> build() {
            List<Component> list = new ArrayList<>();
            for (var component : components) {
                component.resolve(list);
            }
            return list;
        }

        /**
         * Adds a blank line to the tooltip.
         * <p>
         * Instead of adding an empty {@link TextComponent}, this method adds a line with a single space
         * to enforce the blank line because auto line breaks are causing issues and remove the blank line.
         *
         * @return the instance of the tooltip builder
         */
        public Tooltip blank() {
            return component(new TextComponent(" "));
        }

        public Tooltip blank(BooleanSupplier condition) {
            components.add(new IfComponent(condition, new TextComponent(" ")));
            return this;
        }

        /**
         * Adds a title component to the tooltip.
         * <p>
         * It uses golden text color.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the title
         * @return the instance of the tooltip builder
         */
        public Tooltip title(String key, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GOLD), replacements);
        }

        /**
         * Adds a conditional title component to the tooltip.
         * <p>
         * It uses golden text color.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param condition    the condition to check
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the title
         * @return the instance of the tooltip builder
         */
        public Tooltip title(BooleanSupplier condition, String key, Supplier<?>... replacements) {
            components.add(new IfComponent(
                condition,
                TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GOLD),
                replacements
            ));
            return this;
        }

        /**
         * Adds a line component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(String key, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.WHITE), replacements);
        }

        /**
         * Adds a colored line component to the tooltip.
         *
         * @param key          the key for the translation
         * @param color        the color of the line
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(String key, ChatFormatting color, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, color), replacements);
        }

        /**
         * Adds a conditional line component to the tooltip.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param condition    the condition to check
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(BooleanSupplier condition, String key, Supplier<?>... replacements) {
            components.add(new IfComponent(
                condition,
                TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.WHITE),
                replacements
            ));
            return this;
        }

        /**
         * Adds a conditional colored line component to the tooltip.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param condition    the condition to check
         * @param key          the key for the translation
         * @param color        the color of the line
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(BooleanSupplier condition, String key, ChatFormatting color, Supplier<?>... replacements) {
            components.add(new IfComponent(
                condition,
                TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, color),
                replacements
            ));
            return this;
        }

        public Tooltip lineEnum(
            TRANSLATE_TYPE type, ChatFormatting color, Enum<?> e
        ) {
            return component(TextUtil.translate(type, e.toString().toLowerCase(), color));
        }

        /**
         * Adds a key-value pair component to the tooltip.
         * <p>
         * The format will be "Key: Value".
         * The key and the colon are using green text. The value is white.
         * <p>
         * The replacements will only be applied to the value.
         * This component uses ".key" and ".value" suffixes on the translation key.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the value
         * @return the instance of the tooltip builder
         */
        public Tooltip keyValue(String key, Supplier<?>... replacements) {
            components.add(new FormatComponent(TextUtil.translate(
                    TRANSLATE_TYPE.TOOLTIP,
                    f("{}.key", key),
                    ChatFormatting.GREEN
                )
                .append(TextUtil.colorize(": ", ChatFormatting.GREEN))
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, f("{}.value", key))), replacements));
            return this;
        }

        /**
         * Adds a conditional key-value pair component to the tooltip.
         * <p>
         * The format will be "Key: Value".
         * The key and the colon are using green text. The value is white.
         * <p>
         * The replacements will only be applied to the value.
         * This component uses ".key" and ".value" suffixes on the translation key.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the value
         * @return the instance of the tooltip builder
         */
        public Tooltip keyValue(BooleanSupplier condition, String key, Supplier<?>... replacements) {
            components.add(new IfComponent(
                condition,
                new FormatComponent(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, f("{}.key", key), ChatFormatting.GREEN)
                    .append(TextUtil.colorize(": ", ChatFormatting.GREEN))
                    .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, f("{}.value", key))), replacements)
            ));
            return this;
        }

        /**
         * Adds a key-enum pair component to the tooltip.
         * <p>
         * The format will be "Key: Enum". The key and the colon are using green text. The enum is white.
         * <p>
         * The enum will use its own translation key.
         *
         * @param key  the key for the translation
         * @param type the enum translation type
         * @param e    the enum
         * @return the instance of the tooltip builder
         */
        public Tooltip keyEnum(String key, TRANSLATE_TYPE type, Supplier<Enum<?>> e) {
            components.add(new EnumComponent(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GREEN)
                .append(TextUtil.colorize(": ", ChatFormatting.GREEN))
                .append(TextComponent.EMPTY), type, e));
            return this;
        }

        /**
         * Adds a click action component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the click action
         * @return the instance of the tooltip builder
         */
        public Tooltip clickAction(String key, Supplier<?>... replacements) {
            return component(TextUtil.colorize("> ", ChatFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_click", ChatFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY)), replacements);
        }

        /**
         * Adds a shift click action component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the shift click action
         * @return the instance of the tooltip builder
         */
        public Tooltip shiftClickAction(String key, Supplier<?>... replacements) {
            return component(TextUtil.colorize("> ", ChatFormatting.GRAY)
                .append(TextUtil.colorize(String.format(
                    "%s + %s",
                    InputConstants.getKey("key.keyboard.left.shift").getDisplayName().getString(),
                    TextUtil.translateAsString(TRANSLATE_TYPE.TOOLTIP, "action_click")
                ), ChatFormatting.AQUA))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY)), replacements);
        }

        /**
         * Adds a hotkey action component to the tooltip.
         *
         * @param hotkey       the hotkey from the {@link InputConstants}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyAction(String hotkey, String key, Supplier<?>... replacements) {
            return component(TextUtil.colorize("> ", ChatFormatting.GRAY)
                .append(TextUtil.colorize(
                    InputConstants.getKey(hotkey).getDisplayName().getString(),
                    ChatFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY)), replacements);
        }

        /**
         * Adds a hotkey hold action component to the tooltip.
         *
         * @param hotkey       the hotkey from the {@link InputConstants}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey hold action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyHoldAction(String hotkey, String key, Supplier<?>... replacements) {
            return component(TextUtil.colorize("> ", ChatFormatting.GRAY)
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", ChatFormatting.GRAY))
                .append(" ")
                .append(TextUtil.colorize(
                    InputConstants.getKey(hotkey).getDisplayName().getString(),
                    ChatFormatting.AQUA
                ))
                .append(" ")
                .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY)), replacements);
        }

        /**
         * Adds a conditional hotkey hold action component to the tooltip.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param hotkey       the hotkey from the {@link InputConstants}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey hold action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyHoldAction(
            BooleanSupplier condition, String hotkey, String key, Supplier<?>... replacements
        ) {
            components.add(new IfComponent(
                condition,
                TextUtil.colorize("> ", ChatFormatting.GRAY)
                    .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", ChatFormatting.GRAY))
                    .append(" ")
                    .append(TextUtil.colorize(
                        InputConstants.getKey(hotkey).getDisplayName().getString(),
                        ChatFormatting.AQUA
                    ))
                    .append(" ")
                    .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, ChatFormatting.GRAY)),
                replacements
            ));
            return this;
        }

        /**
         * Adds a conditional component to the tooltip.
         * <p>
         * Checks a boolean supplier to determine which component
         * should be added.
         *
         * @param logicBuilder the conditional logic builder
         * @return an instance of the tooltip builder
         */
        public Tooltip conditional(Consumer<? super LogicComponent> logicBuilder) {
            var logic = new LogicComponent();
            logicBuilder.accept(logic);
            logic.validate();
            components.add(logic);
            return this;
        }

        /**
         * Adds a raw component to the tooltip.
         * <p>
         * Can be used if the exact component type is not covered by the builder.
         *
         * @param component    the component to add
         * @param replacements the optional replacements to apply to the component
         * @return the instance of the tooltip builder
         */
        private Tooltip component(Component component, Supplier<?>... replacements) {
            components.add(new TooltipComponent(component, replacements));
            return this;
        }

        public static class TooltipComponent extends TextComponent {

            @Nullable final Component component;
            final Supplier<?>[] replacements;

            TooltipComponent(@Nullable Component component, Supplier<?>... replacements) {
                super("");
                this.component = component;
                this.replacements = replacements;
            }

            public void resolve(List<? super Component> tooltip) {
                if (component == null) return;
                if (replacements.length > 0 && component instanceof TranslatableComponent translation) {
                    tooltip.add(handleReplacements(translation).copy());
                    return;
                }
                tooltip.add(component);
            }

            TranslatableComponent handleReplacements(TranslatableComponent textComponent) {
                return new TranslatableComponent(
                    textComponent.getKey(),
                    Arrays.stream(replacements).map(Supplier::get).toArray()
                );
            }
        }

        private static final class FormatComponent extends TooltipComponent {

            private FormatComponent(MutableComponent textComponent, Supplier<?>... replacements) {
                super(textComponent, replacements);
            }

            @Override
            public void resolve(List<? super Component> tooltip) {
                assert component != null;
                var value = component.getSiblings().get(1);
                value = handleReplacements((TranslatableComponent) value).withStyle(ChatFormatting.WHITE);
                component.getSiblings().set(1, value);
                tooltip.add(component.copy());
            }
        }

        private static final class EnumComponent extends TooltipComponent {

            private final TRANSLATE_TYPE type;

            private EnumComponent(
                MutableComponent textComponent, TRANSLATE_TYPE type, Supplier<Enum<?>> e
            ) {
                super(textComponent, e);
                this.type = type;
            }

            @Override
            public void resolve(List<? super Component> tooltip) {
                assert component != null;
                component.getSiblings()
                    .set(
                        1,
                        TextUtil.translate(type, replacements[0].get().toString().toLowerCase(), ChatFormatting.WHITE)
                    );
                tooltip.add(component);
            }
        }

        private static final class IfComponent extends TooltipComponent {

            @Nullable private final TooltipComponent tooltipComponent;
            private final BooleanSupplier condition;

            private IfComponent(BooleanSupplier condition, TooltipComponent component) {
                super(component.component, component.replacements);
                this.condition = condition;
                tooltipComponent = component;
            }

            @SuppressWarnings("OverloadedVarargsMethod")
            private IfComponent(BooleanSupplier condition, Component textComponent, Supplier<?>... replacements) {
                super(textComponent, replacements);
                tooltipComponent = null;
                this.condition = condition;
            }

            @Override
            public void resolve(List<? super Component> tooltip) {
                if (!condition.getAsBoolean()) return;
                if (tooltipComponent != null) {
                    tooltipComponent.resolve(tooltip);
                    return;
                }
                super.resolve(tooltip);
            }
        }

        public static final class LogicComponent extends TooltipComponent {

            private BooleanSupplier condition;
            private Tooltip then;
            private Tooltip otherwise;

            private LogicComponent() {
                super(null);
            }

            /**
             * Adds the condition for the logic component.
             *
             * @param condition the boolean supplier to check
             * @return the instance of the logic component builder
             */
            public LogicComponent condition(BooleanSupplier condition) {
                assert this.condition == null : "condition already set";
                this.condition = condition;
                return this;
            }

            /**
             * Adds the component to the tooltip for when the condition is true.
             *
             * @param then the component to add
             * @return the instance of the logic component builder
             */
            public LogicComponent then(Tooltip then) {
                assert this.then == null : "then condition already set";
                this.then = then;
                return this;
            }

            /**
             * Adds the component to the tooltip for when the condition is false.
             *
             * @param otherwise the component to add
             * @return the instance of the logic component builder
             */
            public LogicComponent otherwise(Tooltip otherwise) {
                assert this.otherwise == null : "otherwise condition already set";
                this.otherwise = otherwise;
                return this;
            }

            @Override
            public void resolve(List<? super Component> tooltip) {
                if (condition.getAsBoolean()) {
                    tooltip.addAll(then.build());
                } else {
                    tooltip.addAll(otherwise.build());
                }
            }

            private void validate() {
                assert condition != null : "condition not set";
                assert then != null : "then case not set";
                assert otherwise != null : "otherwise case not set";
            }
        }
    }
}
