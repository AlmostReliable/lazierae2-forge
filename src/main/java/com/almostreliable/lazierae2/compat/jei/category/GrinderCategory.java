package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.GRINDER_ID;

public class GrinderCategory extends SingleInputCategory {

    private static final ResourceLocation UID = TextUtil.getRL(GRINDER_ID);
    public static final RecipeType<SingleInputRecipe> TYPE = new RecipeType<>(UID, SingleInputRecipe.class);

    public GrinderCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, GRINDER_ID, Blocks.GRINDER.get());
    }

    @Override
    public RecipeType<SingleInputRecipe> getRecipeType() {
        return TYPE;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
