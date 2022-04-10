package com.github.almostreliable.lazierae2.compat.jei.category;

import com.github.almostreliable.lazierae2.core.Setup.Blocks;
import com.github.almostreliable.lazierae2.util.TextUtil;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.util.ResourceLocation;

import static com.github.almostreliable.lazierae2.core.Constants.ENERGIZER_ID;

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
