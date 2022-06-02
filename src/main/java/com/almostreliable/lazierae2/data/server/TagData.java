package com.almostreliable.lazierae2.data.server;

import appeng.core.definitions.AEItems;
import com.almostreliable.lazierae2.core.Setup;
import com.almostreliable.lazierae2.core.Setup.Blocks;
import com.almostreliable.lazierae2.core.Setup.Blocks.Assembler;
import com.almostreliable.lazierae2.core.Setup.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE;

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
            tag(Setup.Tags.Items.DUSTS_RESONATING).add(Items.RESONATING_DUST.get());
            tag(Tags.Items.DUSTS).add(Items.COAL_DUST.get(), Items.CARB_FLUIX_DUST.get(), Items.RESONATING_DUST.get());

            tag(Setup.Tags.Items.GEMS_RESONATING).add(Items.RESONATING_CRYSTAL.get());
            tag(Tags.Items.GEMS).add(Items.RESONATING_CRYSTAL.get());

            tag(Setup.Tags.Items.INGOTS_FLUIX_STEEL).add(Items.FLUIX_STEEL.get());
            tag(Tags.Items.INGOTS).add(Items.FLUIX_STEEL.get());

            tag(Setup.Tags.Items.PROCESSOR_PARALLEL).add(Items.PARALLEL_PROCESSOR.get());
            tag(Setup.Tags.Items.PROCESSOR_SPEC).add(Items.SPEC_PROCESSOR.get());
            tag(Setup.Tags.Items.PROCESSORS).add(
                Items.PARALLEL_PROCESSOR.get(),
                Items.SPEC_PROCESSOR.get(),
                AEItems.CALCULATION_PROCESSOR.asItem(),
                AEItems.ENGINEERING_PROCESSOR.asItem(),
                AEItems.LOGIC_PROCESSOR.asItem()
            );
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
            tag(Setup.Tags.Blocks.MACHINES).add(
                Blocks.AGGREGATOR.get(),
                Blocks.ETCHER.get(),
                Blocks.GRINDER.get(),
                Blocks.INFUSER.get(),
                Blocks.REQUESTER.get()
            );
            tag(MINEABLE_WITH_PICKAXE).addTag(Setup.Tags.Blocks.MACHINES).add(
                Assembler.CONTROLLER.get(),
                Assembler.ACCELERATOR.get(),
                Assembler.TIER_1.get(),
                Assembler.TIER_2.get(),
                Assembler.TIER_3.get(),
                Assembler.WALL.get(),
                Assembler.FRAME.get()
            );
        }
    }
}
