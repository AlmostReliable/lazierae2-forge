package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.gui.MachineScreen;
import com.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
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

public abstract class TripleInputCategory extends MachineCategory<TripleInputRecipe> {

    private final IDrawable background;
    private final IDrawable slot;

    TripleInputCategory(IGuiHelper guiHelper, String id, IItemProvider iconProvider) {
        super(guiHelper, id, iconProvider);
        ResourceLocation backgroundTexture = MachineScreen.TEXTURE;
        background = guiHelper
            .drawableBuilder(backgroundTexture, 42, 6, 92, 62)
            .setTextureSize(MachineScreen.TEXTURE_WIDTH, MachineScreen.TEXTURE_HEIGHT)
            .build();
        slot = guiHelper
            .drawableBuilder(backgroundTexture, 43, 28, MachineScreen.SLOT_SIZE, MachineScreen.SLOT_SIZE)
            .setTextureSize(MachineScreen.TEXTURE_WIDTH, MachineScreen.TEXTURE_HEIGHT)
            .build();
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, TripleInputRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup menu = recipeLayout.getItemStacks();
        // output
        setupSlot(menu, 0, false, 74, 23);
        // inputs
        setupSlot(menu, 1, true, 2, 2);
        setupSlot(menu, 2, true, 2, 23);
        setupSlot(menu, 3, true, 2, 44);
        super.setRecipe(recipeLayout, recipe, ingredients);
    }

    @Override
    public Class<? extends TripleInputRecipe> getRecipeClass() {
        return TripleInputRecipe.class;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void draw(TripleInputRecipe recipe, MatrixStack matrix, double mX, double mY) {
        // additional input slots
        slot.draw(matrix, 1, 1);
        slot.draw(matrix, 1, 43);
        // progress
        progressEmpty.draw(matrix, 36, 17);
        progress.draw(matrix, 36, 17);
        // required energy
        String energy = TextUtil.formatEnergy(recipe.getEnergyCost(), 1, 3, false, true);
        GuiUtil.renderText(matrix, energy, ANCHOR.TOP_RIGHT, 91, 46, 0.8f, 0x00_0000);
        // required time
        String time = f("{} ticks", recipe.getProcessTime());
        GuiUtil.renderText(matrix, time, ANCHOR.TOP_RIGHT, 91, 54, 0.8f, 0x00_0000);
    }
}
