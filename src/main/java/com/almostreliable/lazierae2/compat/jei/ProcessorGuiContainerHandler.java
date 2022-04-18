package com.almostreliable.lazierae2.compat.jei;

import com.almostreliable.lazierae2.gui.ProcessorScreen;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProcessorGuiContainerHandler implements IGuiContainerHandler<ProcessorScreen> {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final ResourceLocation[] categories;

    ProcessorGuiContainerHandler(int x, int y, int width, int height, ResourceLocation... categories) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.categories = categories;
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
        ProcessorScreen screen, double mX, double mY
    ) {
        return Collections.singleton(createClickArea(screen, x, y, width, height, categories));
    }

    @OnlyIn(Dist.CLIENT)
    private IGuiClickableArea createClickArea(
        ProcessorScreen screen, int x, int y, int width, int height, ResourceLocation... categories
    ) {
        var clickableArea = new Rect2i(x, y, width, height);
        var type = screen.getMenu().entity.getProcessorType();

        return new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return clickableArea;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                List<ResourceLocation> list = new ArrayList<>();
                for (var category : categories) {
                    if (category.getPath().equals(type.getId())) list.add(category);
                }
                recipesGui.showCategories(list);
            }
        };
    }
}
