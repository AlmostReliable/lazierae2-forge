package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.almostreliable.lazierae2.gui.MachineScreen;
import com.almostreliable.lazierae2.recipe.type.MachineRecipe;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import static com.almostreliable.lazierae2.util.TextUtil.f;

public abstract class MachineCategory<R extends MachineRecipe> implements IRecipeCategory<R> {

    final IDrawable progressEmpty;
    final IDrawableAnimated progress;
    private final String id;
    private final IDrawable icon;

    MachineCategory(IGuiHelper guiHelper, String id, ItemLike iconProvider) {
        this.id = id;
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(iconProvider));

        var progressTexture = TextUtil.getRL(f("textures/gui/progress/{}.png", id));
        progressEmpty = guiHelper
            .drawableBuilder(progressTexture, 0, 0, MachineScreen.PROGRESS_WIDTH / 2, MachineScreen.PROGRESS_HEIGHT)
            .setTextureSize(MachineScreen.PROGRESS_WIDTH, MachineScreen.PROGRESS_HEIGHT)
            .build();
        var progressDrawable = guiHelper.drawableBuilder(
            progressTexture,
            MachineScreen.PROGRESS_WIDTH / 2,
            0,
            MachineScreen.PROGRESS_WIDTH / 2,
            MachineScreen.PROGRESS_HEIGHT
        ).setTextureSize(MachineScreen.PROGRESS_WIDTH, MachineScreen.PROGRESS_HEIGHT).build();
        progress = guiHelper.createAnimatedDrawable(progressDrawable, 80, StartDirection.LEFT, false);
    }

    @Override
    public Component getTitle() {
        return TextUtil.translate(TRANSLATE_TYPE.BLOCK, id);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
