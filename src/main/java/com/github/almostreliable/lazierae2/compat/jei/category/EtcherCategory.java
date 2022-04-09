package com.github.almostreliable.lazierae2.compat.jei.category;

import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import com.github.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

import static com.github.almostreliable.lazierae2.core.Constants.ETCHER_ID;

public class EtcherCategory extends MachineCategory<TripleInputRecipe> {

    public static final ResourceLocation UID = TextUtil.getRL(ETCHER_ID);

    public EtcherCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, ETCHER_ID, Blocks.ETCHER.get());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends TripleInputRecipe> getRecipeClass() {
        return TripleInputRecipe.class;
    }
}
