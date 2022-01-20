package com.github.almostreliable.lazierae2.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class ItemModelData extends ItemModelProvider {

    public ItemModelData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        existingParent(AGGREGATOR_ID);
        existingParent(CENTRIFUGE_ID);
        existingParent(ENERGIZER_ID);
        existingParent(ETCHER_ID);
    }

    /**
     * Creates item data from an existing block parent.
     * <p>
     * This is mostly used for block items.
     *
     * @param id the id of the item and the parent block to get the data from
     */
    private void existingParent(String id) {
        ResourceLocation parentLocation = new ResourceLocation(MOD_ID, "block/" + id);
        withExistingParent(id, parentLocation);
    }
}
