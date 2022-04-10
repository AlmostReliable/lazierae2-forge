package com.github.almostreliable.lazierae2.compat.jei;

import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.machine.MachineType;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MachineGuiContainerHandler implements IGuiContainerHandler<MachineScreen> {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final ResourceLocation[] categories;

    MachineGuiContainerHandler(int x, int y, int width, int height, ResourceLocation... categories) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.categories = categories;
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
        MachineScreen screen, double mX, double mY
    ) {
        return Collections.singleton(createClickArea(screen, x, y, width, height, categories));
    }

    @OnlyIn(Dist.CLIENT)
    private IGuiClickableArea createClickArea(
        MachineScreen screen, int x, int y, int width, int height, ResourceLocation... categories
    ) {
        Rectangle2d clickableArea = new Rectangle2d(x, y, width, height);
        MachineType type = screen.getMenu().tile.getMachineType();

        return new IGuiClickableArea() {
            @Override
            public Rectangle2d getArea() {
                return clickableArea;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                List<ResourceLocation> list = new ArrayList<>();
                for (ResourceLocation category : categories) {
                    if (category.getPath().equals(type.getId())) list.add(category);
                }
                recipesGui.showCategories(list);
            }
        };
    }
}
