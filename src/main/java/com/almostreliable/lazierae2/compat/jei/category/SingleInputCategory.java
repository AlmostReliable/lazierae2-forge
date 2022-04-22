package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.gui.ProcessorScreen;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.util.GuiUtil;
import com.almostreliable.lazierae2.util.GuiUtil.ANCHOR;
import com.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.level.ItemLike;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class SingleInputCategory extends ProcessorCategory<SingleInputRecipe> {

    private final IDrawable background;

    SingleInputCategory(IGuiHelper guiHelper, String id, ItemLike iconProvider) {
        super(guiHelper, id, iconProvider);
        var backgroundTexture = ProcessorScreen.TEXTURE;
        background = guiHelper
            .drawableBuilder(backgroundTexture, 42, 22, 92, 34)
            .setTextureSize(ProcessorScreen.TEXTURE_WIDTH, ProcessorScreen.TEXTURE_HEIGHT)
            .build();
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
    public void setRecipe(IRecipeLayoutBuilder builder, SingleInputRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 7).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 7).addIngredients(recipe.getInputs().get(0));
        super.setRecipe(builder, recipe, focuses);
    }

    @Override
    public void draw(
        SingleInputRecipe recipe, IRecipeSlotsView slotsView, PoseStack stack, double mX, double mY
    ) {
        // progress
        progressEmpty.draw(stack, 36, 1);
        progress.draw(stack, 36, 1);
        // required energy
        var energy = TextUtil.formatEnergy(recipe.getEnergyCost(), 1, 3, false, true);
        GuiUtil.renderText(stack, energy, ANCHOR.TOP_LEFT, 1, 28, 0.8f, 0x00_0000);
        // required time
        var time = f("{} ticks", recipe.getProcessTime());
        GuiUtil.renderText(stack, time, ANCHOR.TOP_RIGHT, 91, 28, 0.8f, 0x00_0000);
    }
}
