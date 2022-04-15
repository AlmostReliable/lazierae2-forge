package com.almostreliable.lazierae2.data.server;

import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Items;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;

public final class TagData {

    private TagData() {}

    public static class ItemTags extends ItemTagsProvider {

        public ItemTags(
            DataGenerator gen, BlockTagsProvider p, @Nullable ExistingFileHelper fileHelper
        ) {
            super(gen, p, MOD_ID, fileHelper);
        }

        @Override
        protected void addTags() {
            tag(Setup.Tags.Items.DUSTS_COAL).add(Items.COAL_DUST.get());
            tag(Setup.Tags.Items.DUSTS_CARBONIC_FLUIX).add(Items.CARB_FLUIX_DUST.get());
            tag(Tags.Items.DUSTS).add(Items.COAL_DUST.get()).add(Items.CARB_FLUIX_DUST.get());

            tag(Setup.Tags.Items.GEMS_RESONATING).add(Items.RESONATING_GEM.get());
            tag(Tags.Items.GEMS).add(Items.RESONATING_GEM.get());

            tag(Setup.Tags.Items.INGOTS_FLUIX_IRON).add(Items.FLUIX_IRON.get());
            tag(Setup.Tags.Items.INGOTS_FLUIX_STEEL).add(Items.FLUIX_STEEL.get());
            tag(Tags.Items.INGOTS).add(Items.FLUIX_IRON.get()).add(Items.FLUIX_STEEL.get());

            tag(Setup.Tags.Items.PROCESSOR_PARALLEL).add(Items.PARALLEL_PROCESSOR.get());
            tag(Setup.Tags.Items.PROCESSOR_SPEC).add(Items.SPEC_PROCESSOR.get());
        }
    }

    public static class BlockTags extends BlockTagsProvider {

        public BlockTags(
            DataGenerator gen, @Nullable ExistingFileHelper fileHelper
        ) {
            super(gen, MOD_ID, fileHelper);
        }

        @Override
        protected void addTags() {
            tag(Setup.Tags.Blocks.AGGREGATOR).add(Blocks.AGGREGATOR.get());
            tag(Setup.Tags.Blocks.CENTRIFUGE).add(Blocks.CENTRIFUGE.get());
            tag(Setup.Tags.Blocks.ENERGIZER).add(Blocks.ENERGIZER.get());
            tag(Setup.Tags.Blocks.ETCHER).add(Blocks.ETCHER.get());
        }
    }
}
