package com.almostreliable.lazierae.client.screen.toolbar;

import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import com.almostreliable.lib.client.gui.WidgetChangeListener;
import com.almostreliable.lib.client.gui.WidgetData;
import com.almostreliable.lib.client.gui.widget.AlmostWidget;
import com.almostreliable.lib.util.Area;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class representing a toolbar holding {@link AlmostWidget}s
 * that are automatically aligned at a specific {@link ToolbarAnchor}.
 * <p>
 * Widgets can be separated into two categories: display widgets and interactable widgets.
 * <p>
 * Widgets held by this toolbar should not be added to the host screen's children list.<br>
 * Instead, this toolbar should be added. Rendering and event delegation will be handled
 * automatically.
 */
public class AnchoredToolbar implements Widget, ContainerEventHandler, NarratableEntry, WidgetChangeListener {

    private final ToolbarHost host;
    private final ToolbarAnchor anchor;
    private final int padding;
    private final int offsetX;
    private final int offsetY;
    private final List<AlmostWidget<?>> widgets = new ArrayList<>();
    private final List<GuiEventListener> children = new ArrayList<>();

    public AnchoredToolbar(ToolbarHost host, ToolbarAnchor anchor, int padding, int offsetX, int offsetY) {
        this.host = host;
        this.anchor = anchor;
        this.padding = padding;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public AnchoredToolbar(ToolbarHost host, ToolbarAnchor anchor, int padding) {
        this(host, anchor, padding, 0, 0);
    }

    public AnchoredToolbar(ToolbarHost host, ToolbarAnchor anchor) {
        this(host, anchor, 2);
    }

    /**
     * Adds a display widget to the toolbar.
     * <p>
     * Display widgets are not interactable and their only purpose is to show information.
     *
     * @param widget The widget to add.
     * @param <T>    The type of the widget.
     * @return The widget.
     */
    public <T extends AlmostWidget<?>> T displayWidget(T widget) {
        int index = widgets.size();
        WidgetData widgetData = widget.getData();
        Area area = anchor.getArea(host, index, widgetData.getWidth(), widgetData.getHeight(), padding, offsetX, offsetY);
        widgetData.applyArea(area);
        widgetData.setParent(this);
        widgets.add(widget);
        return widget;
    }

    /**
     * Adds an interactable widget to the toolbar.
     * <p>
     * Interactable widgets receive mouse events and can be interacted with.
     *
     * @param widget The widget to add.
     * @param <T>    The type of the widget.
     * @return The widget.
     */
    public <T extends AlmostWidget<?> & GuiEventListener> T interactableWidget(T widget) {
        children.add(widget);
        return displayWidget(widget);
    }

    public void clear() {
        widgets.forEach(w -> w.getData().setParent(null));
        widgets.clear();
        children.clear();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (var widget : widgets) {
            if (!widget.getData().isVisible()) continue;
            widget.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean isDragging() {
        return false;
    }

    @Override
    public void setDragging(boolean isDragging) {

    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void onWidgetVisibilityChanged(AlmostWidget<?> widget) {
        repositionWidgets();
    }

    private void repositionWidgets() {
        int index = 0;
        for (var widget : widgets) {
            WidgetData data = widget.getData();
            if (!data.isVisible()) continue;
            Area area = anchor.getArea(host, index, data.getWidth(), data.getHeight(), padding, offsetX, offsetY);
            data.applyArea(area);
            index++;
        }
    }
}
