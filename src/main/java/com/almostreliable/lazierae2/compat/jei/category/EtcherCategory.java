package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.ETCHER_ID;

public class EtcherCategory extends TripleInputCategory {

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
}
