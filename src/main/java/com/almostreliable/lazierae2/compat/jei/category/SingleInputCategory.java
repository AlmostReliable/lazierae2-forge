package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.gui.MachineScreen;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.util.GuiUtil;
import com.almostreliable.lazierae2.util.GuiUtil.ANCHOR;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class SingleInputCategory extends MachineCategory<SingleInputRecipe> {

    private final IDrawable background;

    SingleInputCategory(IGuiHelper guiHelper, String id, IItemProvider iconProvider) {
        super(guiHelper, id, iconProvider);
        ResourceLocation backgroundTexture = MachineScreen.TEXTURE;
        background = guiHelper
            .drawableBuilder(backgroundTexture, 42, 22, 92, 34)
            .setTextureSize(MachineScreen.TEXTURE_WIDTH, MachineScreen.TEXTURE_HEIGHT)
            .build();
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SingleInputRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup menu = recipeLayout.getItemStacks();
        // output
        setupSlot(menu, 0, false, 74, 7);
        // input
        setupSlot(menu, 1, true, 2, 7);
        super.setRecipe(recipeLayout, recipe, ingredients);
    }

    @Override
    public Class<? extends SingleInputRecipe> getRecipeClass() {
        return SingleInputRecipe.class;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void draw(SingleInputRecipe recipe, MatrixStack matrix, double mX, double mY) {
        // progress
        progressEmpty.draw(matrix, 36, 1);
        progress.draw(matrix, 36, 1);
        // required energy
        String energy = TextUtil.formatEnergy(recipe.getEnergyCost(), 1, 3, false, true);
        GuiUtil.renderText(matrix, energy, ANCHOR.TOP_LEFT, 1, 28, 0.8f, 0x00_0000);
        // required time
        String time = f("{} ticks", recipe.getProcessTime());
        GuiUtil.renderText(matrix, time, ANCHOR.TOP_RIGHT, 91, 28, 0.8f, 0x00_0000);
    }
}
