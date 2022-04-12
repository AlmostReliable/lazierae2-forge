package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.CENTRIFUGE_ID;

public class CentrifugeCategory extends SingleInputCategory {

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
}
