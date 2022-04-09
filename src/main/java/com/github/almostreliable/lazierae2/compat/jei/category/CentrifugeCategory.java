package com.github.almostreliable.lazierae2.compat.jei.category;

import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.recipe.type.SingleInputRecipe;
import com.github.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

import static com.github.almostreliable.lazierae2.core.Constants.CENTRIFUGE_ID;

public class CentrifugeCategory extends MachineCategory<SingleInputRecipe> {

    public static final ResourceLocation UID = TextUtil.getRL(CENTRIFUGE_ID);

    public CentrifugeCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, CENTRIFUGE_ID, Blocks.CENTRIFUGE.get());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends SingleInputRecipe> getRecipeClass() {
        return SingleInputRecipe.class;
    }
}
