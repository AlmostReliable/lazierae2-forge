package com.almostreliable.lazierae.data;

import com.almostreliable.lazierae.LazierAE2;

import net.minecraftforge.data.event.GatherDataEvent;

public final class DataGeneration {

    private DataGeneration() {
    }

    public static void init(GatherDataEvent event) {
        var generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(true, new LazierRecipes(generator));
        }
        if (event.includeClient()) {
            LazierLang.init();
        }

        LazierAE2.MANAGER.dataGen().collectProviders(event.getGenerator());
    }
}
