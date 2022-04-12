package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.AGGREGATOR_ID;

public class AggregatorCategory extends TripleInputCategory {

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
}
