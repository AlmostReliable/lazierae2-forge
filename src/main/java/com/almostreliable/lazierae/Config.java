package com.almostreliable.lazierae;

import com.almostreliable.lib.config.ConfigBuilder;

import java.util.List;

public class Config {

    public static final int MAX_UPGRADES = 8;

    public final Injector injector;

    Config(ConfigBuilder builder) {
        injector = new Injector(builder.category("crystal_injector", "Crystal Injector"));
    }

    public static final class Injector {

        public final List<Double> energyValues;
        public final List<Double> speedValues;

        private Injector(ConfigBuilder builder) {
            energyValues = builder.doubleListValue("energy_values", List.of(1.1, 1.25, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0))
                .minLength(MAX_UPGRADES, () -> 1.0)
                .maxLength(MAX_UPGRADES)
                .read();
            speedValues = builder.doubleListValue("speed_values", List.of(1.1, 1.25, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0))
                .minLength(MAX_UPGRADES, () -> 1.0)
                .maxLength(MAX_UPGRADES)
                .read();
        }
    }
}
