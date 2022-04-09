package com.github.almostreliable.lazierae2.compat.jei.category;

import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.recipe.type.TripleInputRecipe;
import com.github.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

import static com.github.almostreliable.lazierae2.core.Constants.AGGREGATOR_ID;

public class AggregatorCategory extends MachineCategory<TripleInputRecipe> {

    public static final ResourceLocation UID = TextUtil.getRL(AGGREGATOR_ID);

    public AggregatorCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, AGGREGATOR_ID, Blocks.AGGREGATOR.get());
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
