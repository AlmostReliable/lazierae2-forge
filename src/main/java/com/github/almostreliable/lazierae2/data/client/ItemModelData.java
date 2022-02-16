package com.github.almostreliable.lazierae2.data.client;

import com.github.almostreliable.lazierae2.core.Setup.Items;
import com.github.almostreliable.lazierae2.recipe.MachineType;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;
import java.util.function.Supplier;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public class ItemModelData extends ItemModelProvider {

    public ItemModelData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        existingParent(MachineType.AGGREGATOR.getId());
        existingParent(MachineType.CENTRIFUGE.getId());
        existingParent(MachineType.ENERGIZER.getId());
        existingParent(MachineType.ETCHER.getId());

        builder(Items.CARB_FLUIX_DUST);
        builder(Items.COAL_DUST);
        builder(Items.FLUIX_IRON);
        builder(Items.FLUIX_STEEL);
        builder(Items.GROWTH_CHAMBER);
        builder(Items.LOGIC_UNIT);
        builder(Items.PARALLEL_PRINTED);
        builder(Items.PARALLEL_PROCESSOR);
        builder(Items.RESONATING_GEM);
        builder(Items.SPEC_CORE_1);
        builder(Items.SPEC_CORE_2);
        builder(Items.SPEC_CORE_4);
        builder(Items.SPEC_CORE_8);
        builder(Items.SPEC_CORE_16);
        builder(Items.SPEC_CORE_32);
        builder(Items.SPEC_CORE_64);
        builder(Items.SPEC_PRINTED);
        builder(Items.SPEC_PROCESSOR);
        builder(Items.UNIVERSAL_PRESS);
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

    private void builder(Supplier<? extends Item> item) {
        String name = Objects.requireNonNull(item.get().getRegistryName()).getPath();
        ModelFile itemGenerated = getExistingFile(mcLoc("item/generated"));
        getBuilder(name).parent(itemGenerated).texture("layer0", "item/" + name);
    }
}
