package com.almostreliable.lazierae2.data;

import com.almostreliable.lazierae2.data.client.BlockStateData;
import com.almostreliable.lazierae2.data.client.ItemModelData;
import com.almostreliable.lazierae2.data.server.RecipeData;
import com.almostreliable.lazierae2.data.server.TagData.BlockTags;
import com.almostreliable.lazierae2.data.server.TagData.ItemTags;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        var gen = event.getGenerator();
        var fileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            // generate tags
            var blockTags = new BlockTags(gen, fileHelper);
            gen.addProvider(blockTags);
            gen.addProvider(new ItemTags(gen, blockTags, fileHelper));
            // generate recipes
            gen.addProvider(new RecipeData(gen));
        }
        if (event.includeClient()) {
            // generate block states and block models
            gen.addProvider(new BlockStateData(gen, fileHelper));
            // generate item models
            gen.addProvider(new ItemModelData(gen, fileHelper));
        }
    }
}
