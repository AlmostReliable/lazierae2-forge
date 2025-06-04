package com.almostreliable.lazierae2.fabric.data;

import com.almostreliable.lazierae2.LazierAE2;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class DataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        LazierAE2.MANAGER.dataGen().collectProviders(gen);
        // gen.addProvider(ModelData::new);
        // gen.addProvider(LocalizationData::new);
    }
}
