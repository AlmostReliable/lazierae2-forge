package com.almostreliable.lazierae2.forge.data;

import com.almostreliable.lazierae2.LazierAE2;
import net.minecraftforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {}

    public static void init(GatherDataEvent event) {
        LazierAE2.MANAGER.dataGen().collectProviders(event.getGenerator());
    }
}
