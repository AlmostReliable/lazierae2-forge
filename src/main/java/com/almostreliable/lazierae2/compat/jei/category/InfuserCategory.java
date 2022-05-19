package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.INFUSER_ID;

public class InfuserCategory extends TripleInputCategory {

    public static final ResourceLocation UID = TextUtil.getRL(INFUSER_ID);

    public InfuserCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, INFUSER_ID, Blocks.INFUSER.get());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
