package com.github.almostreliable.lazierae2.compat.jei.category;

import com.github.almostreliable.lazierae2.component.InventoryHandler;
import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.github.almostreliable.lazierae2.gui.MachineScreen;
import com.github.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.github.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.github.almostreliable.lazierae2.util.TextUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import static com.github.almostreliable.lazierae2.util.TextUtil.f;

public abstract class MachineCategory<R extends MachineRecipe> implements IRecipeCategory<R> {

    private final String id;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;

    MachineCategory(IGuiHelper guiHelper, String id, IItemProvider iconProvider) {
        this.id = id;
        icon = guiHelper.createDrawableIngredient(new ItemStack(iconProvider));

        ResourceLocation backgroundTexture = TextUtil.getRL(f("textures/jei/{}.png", id));
        ResourceLocation progressTexture = TextUtil.getRL(f("textures/gui/progress/{}.png", id));
        background = guiHelper.drawableBuilder(backgroundTexture, 0, 0, 90, 60).setTextureSize(90, 60).build();
        IDrawableStatic progressDrawable = guiHelper.drawableBuilder(
            progressTexture,
            MachineScreen.PROGRESS_WIDTH / 2,
            0,
            MachineScreen.PROGRESS_WIDTH / 2,
            MachineScreen.PROGRESS_HEIGHT
        ).setTextureSize(MachineScreen.PROGRESS_WIDTH, MachineScreen.PROGRESS_HEIGHT).build();
        progress = guiHelper.createAnimatedDrawable(progressDrawable, 80, StartDirection.LEFT, false);
    }

    /**
     * Utility method to create an item slot for the JEI recipe category.
     * <p>
     * For legacy reasons, JEI uses 18x18 dimensions for slots, so we need to
     * subtract 1 from the width and height.
     * <p>
     * The slot index uses the output slot index as entry point.
     *
     * @param menu    The menu to add the slot to.
     * @param slot    The slot index.
     * @param isInput Whether the slot is an input slot.
     * @param x       The x position of the slot.
     * @param y       The y position of the slot.
     */
    private void setupSlot(IGuiItemStackGroup menu, int slot, boolean isInput, int x, int y) {
        menu.init(InventoryHandler.OUTPUT_SLOT + slot, isInput, x - 1, y - 1);
    }

    @Override
    public String getTitle() {
        return I18n.get(TextUtil.translate(TRANSLATE_TYPE.BLOCK, id).getKey());
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(R recipe, IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getResultItem());
        ingredients.setInputIngredients(recipe.getInputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, R recipe, IIngredients ingredients) {
        IGuiItemStackGroup menu = recipeLayout.getItemStacks();
        // output
        setupSlot(menu, 0, false, 73, 22);
        // inputs
        if (recipe instanceof SingleInputRecipe) {
            setupSlot(menu, 1, true, 1, 22);
        } else {
            setupSlot(menu, 1, true, 1, 1);
            setupSlot(menu, 2, true, 1, 22);
            setupSlot(menu, 3, true, 1, 43);
        }
        // apply ingredients to slots
        menu.set(ingredients);
    }

    @Override
    public void draw(R recipe, MatrixStack matrix, double mX, double mY) {
        // draw progress
        progressBackground.draw(matrix, 78, 24);
        progress.draw(matrix, 78, 24);
    }
}
