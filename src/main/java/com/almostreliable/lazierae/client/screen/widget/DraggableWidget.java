package com.almostreliable.lazierae.client.screen.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.almostreliable.lib.client.gui.AlmostGuiEventListener;
import com.almostreliable.lib.client.gui.WidgetData;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;
import com.almostreliable.lib.client.rendering.AlmostRendering;
import com.almostreliable.lib.util.Area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

public class DraggableWidget implements AlmostWidget<WidgetData>, AlmostGuiEventListener<WidgetData> {

    private static final long DEFAULT_WIDGET_COLOR = 0xFFAB_ABABL;
    private static final long DEFAULT_SHADOW_COLOR = 0x8000_0000L;
    private static final int DEFAULT_SHADOW_DISTANCE = 2;
    private static final GuiEventListener EMPTY_LISTENER = new GuiEventListener() {};

    // widget data
    private final Screen screen;
    private final WidgetData data;
    private final AlmostWidget<?> childWidget;
    private final GuiEventListener childListener;
    private final Area.Mutable headerArea;
    private final int borderStrength;

    // widget options
    private double depth;
    @Nullable
    private Component title;
    private long borderColor = DEFAULT_WIDGET_COLOR;
    private long headerColor = DEFAULT_WIDGET_COLOR;
    private ShadowData shadowData = ShadowData.NONE;
    private boolean closeControl;
    private boolean resetControl;

    // dragging
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public static DraggableWidget of(Screen screen, AlmostWidget<?> childWidget, int headerHeight, int borderStrength) {
        GuiEventListener childListener = childWidget instanceof GuiEventListener listener ? listener : EMPTY_LISTENER;

        // encapsulate the child widget and the border
        var data = WidgetData.of(childWidget.getData().expand(borderStrength));
        // create the header area above the child widget
        var headerArea = Area.mutableOf(data.getX(), childWidget.getData().getY() - headerHeight, data.getWidth(), headerHeight);
        // enlarge the data to include the header
        data.applyArea(data.union(headerArea));

        return new DraggableWidget(screen, data, childWidget, childListener, headerArea, borderStrength);
    }

    private DraggableWidget(
        Screen screen, WidgetData data, AlmostWidget<?> childWidget, GuiEventListener childListener, Area.Mutable headerArea,
        int borderStrength
    ) {
        this.screen = screen;
        this.data = data;
        this.childWidget = childWidget;
        this.childListener = childListener;
        this.headerArea = headerArea;
        this.borderStrength = borderStrength;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        AlmostWidget.super.render(poseStack, mouseX, mouseY, delta);

        RenderSystem.enableDepthTest();
        poseStack.pushPose();
        poseStack.translate(0, 0, depth);

        if (shadowData != ShadowData.NONE) {
            AlmostRendering.fill(poseStack, shadowData.area, shadowData.color);
        }

        childWidget.render(poseStack, mouseX, mouseY, delta);

        AlmostRendering.innerBorder(poseStack, data, borderColor, borderStrength);
        AlmostRendering.fill(poseStack, headerArea, headerColor);
        // TODO: render controls

        // render the title on the header
        if (title != null) {
            renderTitle(poseStack, title);
        }

        poseStack.popPose();
        RenderSystem.disableDepthTest();
    }

    private void renderTitle(PoseStack poseStack, Component title) {
        var font = Minecraft.getInstance().font;
        var titleWidth = font.width(title);
        var titleHeight = font.lineHeight;

        // scale the matrix so that the text fits in the header
        var widthScale = Math.min(1, (float) headerArea.getWidth() / titleWidth);
        var heightScale = Math.min(1, (float) headerArea.getHeight() / titleHeight);
        var titleScale = Math.min(widthScale, heightScale);
        var titleX = (headerArea.getX() + 2) / titleScale;
        var titleY = (headerArea.getY() + headerArea.getHeight() - titleHeight) / titleScale;

        poseStack.pushPose();
        poseStack.translate(0, 0, depth + 1);
        poseStack.scale(titleScale, titleScale, 1);
        // TODO: use component colors
        font.drawShadow(poseStack, title, titleX, titleY, 0xFF_FFFF);
        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return (childWidget.getData().isHovered() && childListener.mouseClicked(mouseX, mouseY, mouseButton)) ||
            AlmostGuiEventListener.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int mouseButton) {
        if (headerArea.inBounds(mouseX, mouseY)) {
            activateDragging(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        childListener.mouseReleased(mouseX, mouseY, mouseButton);
        return AlmostGuiEventListener.super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onRelease(double mouseX, double mouseY, int mouseButton) {
        deactivateDragging();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (dragging && getData().isActive() && getData().isVisible()) {
            var mX = Mth.clamp(mouseX, 0, screen.width);
            var mY = Mth.clamp(mouseY, 0, screen.height);
            updateAreas((int) mX - dragOffsetX, (int) mY - dragOffsetY);
            return true;
        }
        return childListener.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return childWidget.getData().isHovered() && childListener.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void playClickSound(SoundManager soundManager, int mouseButton) {
        // do nothing
    }

    @Override
    public WidgetData getData() {
        return data;
    }

    /**
     * Sets the rendering depth of the widget.
     * <p>
     * This can be used in case the widget clashes with other widgets.
     *
     * @param depth The rendering depth of the widget.
     */
    public DraggableWidget setDepth(double depth) {
        this.depth = depth;
        return this;
    }

    /**
     * Sets the title of the widget.
     * <p>
     * The title is rendered on the header bar of the widget and is automatically scaled.
     *
     * @param title The title of the widget.
     */
    public DraggableWidget setTitle(Component title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the title of the widget.
     * <p>
     * The title is rendered on the header bar of the widget and is automatically scaled.
     *
     * @param title The title of the widget.
     */
    public DraggableWidget setTitle(String title) {
        this.title = Component.literal(title);
        return this;
    }

    /**
     * Sets the color of the widget.
     * <p>
     * The color will be applied to the header bar and the border of the widget.
     *
     * @param widgetColor The color of the widget.
     */
    public DraggableWidget setWidgetColor(long widgetColor) {
        headerColor = widgetColor;
        borderColor = widgetColor;
        return this;
    }

    /**
     * Sets the color of the border.
     * <p>
     * This can be used to colorize the border independently.
     *
     * @param borderColor The color of the border.
     */
    public DraggableWidget setBorderColor(long borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * Sets the color of the header bar.
     * <p>
     * This can be used to colorize the header bar independently.
     *
     * @param headerColor The color of the header bar.
     */
    public DraggableWidget setHeaderColor(long headerColor) {
        this.headerColor = headerColor;
        return this;
    }

    /**
     * Activates the rendering of a shadow behind the widget.
     * <p>
     * The shadow will always be rendered at the bottom right of the widget.
     *
     * @param color    The color of the shadow.
     * @param distance The distance of the shadow.
     */
    public DraggableWidget activateShadow(long color, int distance) {
        shadowData = ShadowData.of(color, distance, data);
        return this;
    }

    /**
     * Activates the rendering of a shadow behind the widget with default values.
     * <p>
     * The shadow will always be rendered at the bottom right of the widget.
     */
    public DraggableWidget activateShadow() {
        activateShadow(DEFAULT_SHADOW_COLOR, DEFAULT_SHADOW_DISTANCE);
        return this;
    }

    /**
     * Deactivates the rendering of the shadow.
     */
    public DraggableWidget deactivateShadow() {
        shadowData = ShadowData.NONE;
        return this;
    }

    /**
     * Enables the close control on the header bar.
     * <p>
     * When the close control button is clicked, the widget will be made invisible
     * and all user inputs will be ignored.
     */
    public DraggableWidget showCloseControl() {
        closeControl = true;
        return this;
    }

    /**
     * Enables the reset control on the header bar.
     * <p>
     * When the reset control button is clicked, the widget will reset to its initial position.
     */
    public DraggableWidget showResetControl() {
        resetControl = true;
        return this;
    }

    private void activateDragging(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = (int) (mouseX - data.getX());
        dragOffsetY = (int) (mouseY - data.getY());
    }

    private void deactivateDragging() {
        dragging = false;
        dragOffsetX = 0;
        dragOffsetY = 0;
    }

    private void updateAreas(int x, int y) {
        headerArea.move(x - headerArea.getX(), y - headerArea.getY());
        data.move(x - data.getX(), y - data.getY());
        var childData = childWidget.getData();
        childData.move(x + borderStrength - childData.getX(), y + headerArea.getHeight() - childData.getY());
        shadowData.moveArea(data);
    }

    private record ShadowData(long color, int distance, Area.Mutable area) {

        private static final ShadowData NONE = new ShadowData(0, 0, Area.mutableOf(0, 0));

        private static ShadowData of(long color, int distance, Area widgetArea) {
            var data = new ShadowData(color, distance, Area.mutableOf(widgetArea.getWidth(), widgetArea.getHeight()));
            data.moveArea(widgetArea);
            return data;
        }

        private void moveArea(Area widgetArea) {
            area.move(widgetArea.getX() + distance - area.getX(), widgetArea.getY() + distance - area.getY());
        }
    }
}
