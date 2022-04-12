package com.github.almostreliable.lazierae2.util;

import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.almostreliable.lazierae2.util.TextUtil.f;

public final class GuiUtil {

    private GuiUtil() {}

    /**
     * Draws a given text at the given position with the given color.
     * <p>
     * This method handles the translation and scaling of the text in order
     * to maintain the same position after the scaling.
     * <p>
     * The {@link ANCHOR} can be used to specify the alignment of the text.
     *
     * @param matrix the matrix stack for the rendering
     * @param text   the text to draw
     * @param anchor the anchor point of the text
     * @param x      the x position
     * @param y      the y position
     * @param scale  the scale of the text
     * @param color  the color of the text as decimal
     */
    public static void renderText(
        MatrixStack matrix, String text, ANCHOR anchor, int x, int y, float scale, int color
    ) {
        matrix.pushPose();
        matrix.translate(x, y, 0);
        matrix.scale(scale, scale, 1);

        int xOffset = 0;
        int yOffset = 0;
        FontRenderer font = Minecraft.getInstance().font;
        int width = font.width(text);
        int height = font.lineHeight;
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

        font.draw(matrix, text, xOffset, yOffset, color);
        matrix.popPose();
    }

    public enum ANCHOR {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    @SuppressWarnings({"java:S2160", "UnusedReturnValue", "unused"})
    public static final class Tooltip {

        private final List<Component> components;

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
        public List<ITextComponent> build() {
            List<ITextComponent> list = new ArrayList<>();
            for (Component component : components) {
                component.resolve(list);
            }
            return list;
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
        public Tooltip component(ITextComponent component, Supplier<?>... replacements) {
            components.add(new Component(component, replacements));
            return this;
        }

        /**
         * Adds a blank line to the tooltip.
         * <p>
         * Instead of adding an empty {@link StringTextComponent}, this method adds a line with a single space
         * to enforce the blank line because auto line breaks are causing issues and remove the blank line.
         *
         * @return the instance of the tooltip builder
         */
        public Tooltip blank() {
            return component(new StringTextComponent(" "));
        }

        public Tooltip blank(BooleanSupplier condition) {
            components.add(new IfComponent(condition, new StringTextComponent(" ")));
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
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GOLD), replacements);
        }

        /**
         * Adds a line component to the tooltip.
         *
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(String key, Supplier<?>... replacements) {
            return component(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE), replacements);
        }

        /**
         * Adds a colored line component to the tooltip.
         *
         * @param key          the key for the translation
         * @param color        the color of the line
         * @param replacements the optional replacements to apply to the line
         * @return the instance of the tooltip builder
         */
        public Tooltip line(String key, TextFormatting color, Supplier<?>... replacements) {
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
            components.add(new IfComponent(condition,
                TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.WHITE),
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
        public Tooltip line(BooleanSupplier condition, String key, TextFormatting color, Supplier<?>... replacements) {
            components.add(new IfComponent(condition,
                TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, color),
                replacements
            ));
            return this;
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
            components.add(new FormatComponent(TextUtil
                .translate(TRANSLATE_TYPE.TOOLTIP, f("{}.key", key), TextFormatting.GREEN)
                .append(TextUtil.colorize(": ", TextFormatting.GREEN))
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
            components.add(new IfComponent(condition,
                new FormatComponent(TextUtil
                    .translate(TRANSLATE_TYPE.TOOLTIP, f("{}.key", key), TextFormatting.GREEN)
                    .append(TextUtil.colorize(": ", TextFormatting.GREEN))
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
            components.add(new EnumComponent(TextUtil
                .translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GREEN)
                .append(TextUtil.colorize(": ", TextFormatting.GREEN))
                .append(StringTextComponent.EMPTY), type, e));
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

        /**
         * Adds a conditional hotkey hold action component to the tooltip.
         * <p>
         * If the condition is false, the component will be skipped completely.
         *
         * @param hotkey       the hotkey from the {@link InputMappings}
         * @param key          the key for the translation
         * @param replacements the optional replacements to apply to the hotkey hold action
         * @return the instance of the tooltip builder
         */
        public Tooltip hotkeyHoldAction(
            BooleanSupplier condition, String hotkey, String key, Supplier<?>... replacements
        ) {
            components.add(new IfComponent(condition,
                TextUtil
                    .colorize("> ", TextFormatting.GRAY)
                    .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, "action_hold", TextFormatting.GRAY))
                    .append(" ")
                    .append(TextUtil.colorize(InputMappings.getKey(hotkey).getDisplayName().getString(),
                        TextFormatting.AQUA
                    ))
                    .append(" ")
                    .append(TextUtil.translate(TRANSLATE_TYPE.TOOLTIP, key, TextFormatting.GRAY)),
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
            LogicComponent logic = new LogicComponent();
            logicBuilder.accept(logic);
            logic.validate();
            components.add(logic);
            return this;
        }

        public static class Component extends StringTextComponent {

            @Nullable
            final ITextComponent textComponent;
            final Supplier<?>[] replacements;

            Component(@Nullable ITextComponent textComponent, Supplier<?>... replacements) {
                super("");
                this.textComponent = textComponent;
                this.replacements = replacements;
            }

            public void resolve(List<? super ITextComponent> tooltip) {
                if (textComponent == null) return;
                if (replacements.length > 0 && textComponent instanceof TranslationTextComponent) {
                    tooltip.add(handleReplacements((TranslationTextComponent) textComponent));
                    return;
                }
                tooltip.add(textComponent);
            }

            TranslationTextComponent handleReplacements(TranslationTextComponent textComponent) {
                return new TranslationTextComponent(textComponent.getKey(),
                    Arrays.stream(replacements).map(Supplier::get).toArray()
                );
            }
        }

        private static final class FormatComponent extends Component {

            private FormatComponent(IFormattableTextComponent textComponent, Supplier<?>... replacements) {
                super(textComponent, replacements);
            }

            @Override
            public void resolve(List<? super ITextComponent> tooltip) {
                assert textComponent != null;
                ITextComponent value = textComponent.getSiblings().get(1);
                value = handleReplacements((TranslationTextComponent) value).withStyle(TextFormatting.WHITE);
                textComponent.getSiblings().set(1, value);
                tooltip.add(textComponent);
            }
        }

        private static final class EnumComponent extends Component {

            private final TRANSLATE_TYPE type;

            private EnumComponent(IFormattableTextComponent textComponent, TRANSLATE_TYPE type, Supplier<Enum<?>> e) {
                super(textComponent, e);
                this.type = type;
            }

            @Override
            public void resolve(List<? super ITextComponent> tooltip) {
                assert textComponent != null;
                textComponent
                    .getSiblings()
                    .set(1,
                        TextUtil.translate(type, replacements[0].get().toString().toLowerCase(), TextFormatting.WHITE)
                    );
                tooltip.add(textComponent);
            }
        }

        private static final class IfComponent extends Component {

            @Nullable
            private final Component component;
            private final BooleanSupplier condition;

            private IfComponent(BooleanSupplier condition, Component component) {
                super(component.textComponent, component.replacements);
                this.component = component;
                this.condition = condition;
            }

            @SuppressWarnings("OverloadedVarargsMethod")
            private IfComponent(BooleanSupplier condition, ITextComponent textComponent, Supplier<?>... replacements) {
                super(textComponent, replacements);
                component = null;
                this.condition = condition;
            }

            @Override
            public void resolve(List<? super ITextComponent> tooltip) {
                if (!condition.getAsBoolean()) return;
                if (component != null) {
                    component.resolve(tooltip);
                    return;
                }
                super.resolve(tooltip);
            }
        }

        public static final class LogicComponent extends Component {

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
            public void resolve(List<? super ITextComponent> tooltip) {
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
