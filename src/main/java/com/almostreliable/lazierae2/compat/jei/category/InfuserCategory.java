package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.INFUSER_ID;

public class InfuserCategory extends TripleInputCategory {

    private static final ResourceLocation UID = TextUtil.getRL(INFUSER_ID);
    public static final RecipeType<TripleInputRecipe> TYPE = new RecipeType<>(UID, TripleInputRecipe.class);

    public InfuserCategory(IGuiHelper guiHelper) {
        super(guiHelper, INFUSER_ID, Blocks.INFUSER.get());
    }

    @Override
    public RecipeType<TripleInputRecipe> getRecipeType() {
        return TYPE;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
