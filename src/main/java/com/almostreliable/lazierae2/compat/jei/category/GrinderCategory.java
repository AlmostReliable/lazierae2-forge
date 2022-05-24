package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.GRINDER_ID;

public class GrinderCategory extends SingleInputCategory {

    public static final ResourceLocation UID = TextUtil.getRL(GRINDER_ID);

    public GrinderCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, GRINDER_ID, Blocks.GRINDER.get());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
