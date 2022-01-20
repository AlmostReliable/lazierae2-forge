package com.github.almostreliable.lazierae2.data;

import com.github.almostreliable.lazierae2.data.client.BlockStateData;
import com.github.almostreliable.lazierae2.data.client.ItemModelData;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        if (event.includeClient()) {
            // generate block states and block models
            generator.addProvider(new BlockStateData(generator, fileHelper));
            // generate item models
            generator.addProvider(new ItemModelData(generator, fileHelper));
        }
    }
}
