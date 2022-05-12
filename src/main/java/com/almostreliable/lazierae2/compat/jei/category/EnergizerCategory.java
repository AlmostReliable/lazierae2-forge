package com.almostreliable.lazierae2.compat.jei.category;

import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

import static com.almostreliable.lazierae2.core.Constants.Blocks.ENERGIZER_ID;

public class EnergizerCategory extends SingleInputCategory {

    public static final ResourceLocation UID = TextUtil.getRL(ENERGIZER_ID);

    public EnergizerCategory(
        IGuiHelper guiHelper
    ) {
        super(guiHelper, ENERGIZER_ID, Blocks.ENERGIZER.get());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
