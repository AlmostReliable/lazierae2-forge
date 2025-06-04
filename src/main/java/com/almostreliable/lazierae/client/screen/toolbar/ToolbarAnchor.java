package com.almostreliable.lazierae.client.screen.toolbar;

import com.almostreliable.lib.util.Area;

public enum ToolbarAnchor {

    TOP_LEFT((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + idx * (width + padding);
        int y = host.getGuiTop() - height;
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    TOP_RIGHT((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + host.getXSize() - idx * (width + padding) - width;
        int y = host.getGuiTop() - height;
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    LEFT_TOP((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() - width;
        int y = host.getGuiTop() + idx * (height + padding);
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    LEFT_BOTTOM((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() - width;
        int y = host.getGuiTop() + host.getYSize() - idx * (height + padding) - height;
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    RIGHT_TOP((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + host.getXSize();
        int y = host.getGuiTop() + idx * (height + padding);
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    RIGHT_BOTTOM((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + host.getXSize();
        int y = host.getGuiTop() + host.getYSize() - idx * (height + padding) - height;
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    BOTTOM_LEFT((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + idx * (width + padding);
        int y = host.getGuiTop() + host.getYSize();
        return Area.of(x + offsetX, y + offsetY, width, height);
    }),
    BOTTOM_RIGHT((host, idx, width, height, padding, offsetX, offsetY) -> {
        int x = host.getGuiLeft() + host.getXSize() - idx * (width + padding) - width;
        int y = host.getGuiTop() + host.getYSize();
        return Area.of(x + offsetX, y + offsetY, width, height);
    });

    private final AnchorAreaProvider areaProvider;

    ToolbarAnchor(AnchorAreaProvider areaProvider) {
        this.areaProvider = areaProvider;
    }

    public Area getArea(ToolbarHost host, int idx, int width, int height, int padding, int offsetX, int offsetY) {
        return areaProvider.resolve(host, idx, width, height, padding, offsetX, offsetY);
    }

    @FunctionalInterface
    public interface AnchorAreaProvider {

        Area resolve(ToolbarHost host, int idx, int width, int height, int padding, int offsetX, int offsetY);
    }
}
