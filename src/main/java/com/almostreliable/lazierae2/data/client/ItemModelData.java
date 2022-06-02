package com.almostreliable.lazierae2.data.client;

import appeng.init.client.InitItemModelsProperties;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Blocks.Assembler;
import com.almostreliable.lazierae2.core.Setup.Items;
import com.almostreliable.lazierae2.util.GameUtil;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public class ItemModelData extends ItemModelProvider {

    public ItemModelData(
        DataGenerator gen, ExistingFileHelper fileHelper
    ) {
        super(gen, MOD_ID, fileHelper);
    }

    @Override
    protected void registerModels() {
        withBlockParent(Blocks.AGGREGATOR);
        withBlockParent(Blocks.ETCHER);
        withBlockParent(Blocks.GRINDER);
        withBlockParent(Blocks.INFUSER);
        withBlockParent(Blocks.REQUESTER);
        withBlockParent(Assembler.CONTROLLER);
        withBlockParent(Assembler.ACCELERATOR);
        withBlockParent(Assembler.TIER_1);
        withBlockParent(Assembler.TIER_2);
        withBlockParent(Assembler.TIER_3);
        withBlockParent(Assembler.WALL);
        withBlockParent(Assembler.FRAME);

        genericItem(Items.COAL_DUST);
        genericItem(Items.CARB_FLUIX_DUST);
        genericItem(Items.FLUIX_STEEL);
        genericItem(Items.RESONATING_CRYSTAL);
        crystalSeed(Items.RESONATING_SEED);
        genericItem(Items.RESONATING_DUST);
        genericItem(Items.LOGIC_UNIT);
        genericItem(Items.GROWTH_CORE);
        genericItem(Items.UNIVERSAL_PRESS);
        genericItem(Items.PARALLEL_PRINTED);
        genericItem(Items.SPEC_PRINTED);
        genericItem(Items.PARALLEL_PROCESSOR);
        genericItem(Items.SPEC_PROCESSOR);
        genericItem(Items.SPEC_CORE_1);
        genericItem(Items.SPEC_CORE_2);
        genericItem(Items.SPEC_CORE_4);
        genericItem(Items.SPEC_CORE_8);
        genericItem(Items.SPEC_CORE_16);
        genericItem(Items.SPEC_CORE_32);
        genericItem(Items.SPEC_CORE_64);
    }

    private void withBlockParent(Supplier<? extends Block> block) {
        var id = GameUtil.getIdFromBlock(block.get());
        var parentLocation = TextUtil.getRL(f("block/{}", id));
        withExistingParent(id, parentLocation);
    }

    @SuppressWarnings("SameParameterValue")
    private void crystalSeed(
        Supplier<? extends Item> item
    ) {
        var id = GameUtil.getIdFromItem(item.get());
        var stageOneModel = getModelFile(id + "_2");
        var stageTwoModel = getModelFile(id + "_3");
        getLayerZeroFromParent(id)
            // 2nd growth stage
            .override().predicate(InitItemModelsProperties.GROWTH_PREDICATE_ID, 0.333f).model(stageOneModel).end()
            // 3rd growth stage
            .override().predicate(InitItemModelsProperties.GROWTH_PREDICATE_ID, 0.666f).model(stageTwoModel).end();
    }

    private ItemModelBuilder getLayerZeroFromParent(String id) {
        return getBuilder(id).parent(getItemGenerated()).texture("layer0", f("item/{}", id));
    }

    private void genericItem(Supplier<? extends Item> item) {
        getLayerZeroFromParent(GameUtil.getIdFromItem(item.get()));
    }

    private ModelBuilder<?> getModelFile(String id) {
        return getLayerZeroFromParent(id);
    }

    private ModelFile getItemGenerated() {
        return getExistingFile(mcLoc("item/generated"));
    }
}
