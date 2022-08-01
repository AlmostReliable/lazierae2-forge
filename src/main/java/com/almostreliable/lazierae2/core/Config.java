package com.almostreliable.lazierae2.core;

import com.almostreliable.lazierae2.LazierAE2;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;

import java.util.List;

import static com.almostreliable.lazierae2.core.Constants.Blocks.*;
import static com.almostreliable.lazierae2.core.Constants.MOD_ID;
import static com.almostreliable.lazierae2.util.TextUtil.f;

public final class Config {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        var commonPair = new Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    public static void onConfigReloaded(Reloading event) {
        if (event.getConfig().getModId().equals(MOD_ID)) {
            LazierAE2.LOG.debug("config reloaded");
            List.of(COMMON.aggregator, COMMON.etcher, COMMON.grinder, COMMON.infuser).forEach(config -> {
                var maxUpgrades = config.upgradeSlots.get();
                var energyMultiSize = config.energyUsageMulti.get().size();
                var processTimeMultiSize = config.processTimeMulti.get().size();
                if (maxUpgrades != energyMultiSize) {
                    LazierAE2.LOG.error(f(
                        "Config issue for {} detected! Maximum upgrades are set to {} but energy usage multiplier list has {} entries.",
                        config.id,
                        maxUpgrades,
                        energyMultiSize
                    ));
                }
                if (maxUpgrades != processTimeMultiSize) {
                    LazierAE2.LOG.error(f(
                        "Config issue for {} detected! Maximum upgrades are set to {} but process time multiplier list has {} entries.",
                        config.id,
                        maxUpgrades,
                        processTimeMultiSize
                    ));
                }
            });
        }
    }

    @SuppressWarnings({"TypeParameterExtendsFinalClass", "java:S4968"})
    public static final class ProcessorConfig {

        public final IntValue upgradeSlots;
        public final IntValue baseEnergyBuffer;
        public final IntValue energyBufferAdd;
        public final IntValue baseEnergyUsage;
        public final ConfigValue<List<? extends Double>> energyUsageMulti;
        public final IntValue baseProcessTime;
        public final ConfigValue<List<? extends Double>> processTimeMulti;
        private final String id;

        private ProcessorConfig(Builder builder, String id) {
            this.id = id;
            builder.push(id);
            upgradeSlots = builder.comment(
                f(" The maximum number of upgrades the {} can hold.", id),
                " By default, upgrades will speed up the processor and increase the energy buffer at the cost of a higher energy consumption."
            ).defineInRange("upgrade_slots", 8, 0, 64);

            baseEnergyBuffer = builder.comment(
                f(" The amount of energy the {} can hold.", id),
                " The energy buffer is used to store energy before it is used."
            ).defineInRange("energy_buffer_base", 100_000, 0, Integer.MAX_VALUE);
            energyBufferAdd = builder.comment(
                f(" The additional amount of energy the {} can hold per upgrade.", id),
                " Each upgrade increases the energy buffer by this amount.",
                " Setting this to 0 will disable the energy buffer upgrade.",
                " Calculation: energyBuffer = baseEnergyBuffer + (installedUpgrades * energyBufferAdd)"
            ).defineInRange("energy_buffer_add", 50_000, 0, Integer.MAX_VALUE);

            baseEnergyUsage = builder.comment(
                f(" The base amount of energy the {} uses per tick.", id),
                " Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                " All recipes from the mod itself have an own energy usage value and this setting won't have any effect on them.",
                " This is only useful for custom recipes e.g. for modpack makers."
            ).defineInRange("energy_usage_base", 300, 0, Integer.MAX_VALUE);
            energyUsageMulti = builder.comment(
                    f(" The energy usage multiplier the {} uses per tick per upgrade.", id),
                    " The list index corresponds to the amount of upgrades installed.",
                    " The amount of elements in this list has to be equal to the maximum amount of upgrades.",
                    " Lower values than 1 will decrease the energy consumption, higher values will increase it."
                )
                .defineList(
                    "energy_usage_multi",
                    List.of(1.1, 1.25, 1.5, 2.0, 3.1, 4.5, 6.1, 8.0),
                    Double.class::isInstance
                );

            baseProcessTime = builder.comment(
                f(" The base processing time the {} needs for a recipe.", id),
                " Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify a processing time.",
                " All recipes from the mod itself have an own processing time value and this setting won't have any effect on them.",
                " This is only useful for custom recipes e.g. for modpack makers."
            ).defineInRange("process_time_base", 200, 0, Integer.MAX_VALUE);
            processTimeMulti = builder.comment(
                    f(" The processing time multiplier the {} uses per upgrade.", id),
                    " The list index corresponds to the amount of upgrades installed.",
                    " The amount of elements in this list has to be equal to the maximum amount of upgrades.",
                    " Lower values than 1 will increase the processing speed, higher values will decrease it."
                )
                .defineList(
                    "process_time_multi",
                    List.of(0.94, 0.8, 0.64, 0.5, 0.33, 0.16, 0.09, 0.05),
                    Double.class::isInstance
                );
            builder.pop();
        }
    }

    public static final class CommonConfig {

        public final ProcessorConfig aggregator;
        public final ProcessorConfig etcher;
        public final ProcessorConfig grinder;
        public final ProcessorConfig infuser;

        public final DoubleValue requesterIdleEnergy;
        public final BooleanValue requesterRequireChannel;

        public final DoubleValue assemblerIdleEnergy;
        public final IntValue assemblerQueueSize;
        public final IntValue assemblerWorkPerJob;
        public final IntValue assemblerWorkPerTickBase;
        public final IntValue assemblerWorkPerTickUpgrade;
        public final IntValue assemblerEnergyPerWorkBase;
        public final IntValue assemblerEnergyPerWorkUpgrade;

        public final BooleanValue inWorldResonating;
        public final BooleanValue pressDescription;
        public final BooleanValue singularityDescription;

        private CommonConfig(Builder builder) {
            builder.push(PROCESSOR_ID);
            builder.comment(
                "##################################################################################",
                " This section lets you configure the various values of the mod processors.       #",
                " If there is an option mentioning 'upgrades', AE2 Acceleration Cards are meant.  #",
                "##################################################################################"
            );
            aggregator = new ProcessorConfig(builder, AGGREGATOR_ID);
            grinder = new ProcessorConfig(builder, GRINDER_ID);
            infuser = new ProcessorConfig(builder, INFUSER_ID);
            etcher = new ProcessorConfig(builder, ETCHER_ID);
            builder.pop();

            builder.push(REQUESTER_ID);
            requesterIdleEnergy = builder.comment(f(
                " The energy the {} drains from the ME network when idle.",
                REQUESTER_ID
            )).defineInRange("idle_energy", 5.0, 0.0, Double.MAX_VALUE);
            requesterRequireChannel = builder.comment(f(
                " Whether the {} requires an ME network channel to function.",
                REQUESTER_ID
            )).define("require_channel", true);
            builder.pop();

            builder.push(ASSEMBLER_ID);
            assemblerIdleEnergy = builder.comment(f(
                    " The energy the {} drains from the ME network when idle.",
                    ASSEMBLER_ID
                ))
                .defineInRange("idleEnergy", 5.0, 0.0, Double.MAX_VALUE);
            assemblerQueueSize = builder.comment(f(
                    " The maximum amount of jobs the {} can queue.",
                    ASSEMBLER_ID
                ))
                .defineInRange("queueSize", 64, 1, 1_000);
            assemblerWorkPerJob = builder.comment(f(
                    " The amount of work the {} has to perform per job.",
                    ASSEMBLER_ID
                ))
                .defineInRange("workPerJob", 16, 1, 1_000);
            assemblerWorkPerTickBase = builder.comment(f(
                    " The base amount of work the {} performs per tick.",
                    ASSEMBLER_ID
                ))
                .defineInRange("workPerTickBase", 1, 1, 1_000);
            assemblerWorkPerTickUpgrade = builder.comment(f(
                    " The additional amount of work the {} performs per tick per upgrade.",
                    ASSEMBLER_ID
                ))
                .defineInRange("workPerTickUpgrade", 3, 1, 1_000);
            assemblerEnergyPerWorkBase = builder.comment(f(
                    " The base amount of energy the {} consumes per work.",
                    ASSEMBLER_ID
                ))
                .defineInRange("energyPerWorkBase", 1, 0, 1_000);
            assemblerEnergyPerWorkUpgrade = builder.comment(f(
                    " The additional amount of energy the {} consumes per work per upgrade.",
                    ASSEMBLER_ID
                ))
                .defineInRange("energyPerWorkUpgrade", 3, 0, 1_000);
            builder.pop();

            builder.push("misc");
            inWorldResonating = builder.comment(" Whether the Resonating Dust should be craft-able in the world.")
                .define("inworld_resonating", true);
            pressDescription = builder.comment(
                    " Whether the Universal Press should have a custom JEI info panel which explains that it's only craftable and can't be found in meteorites.")
                .define("press_desc", true);
            singularityDescription = builder.comment(
                    " Whether the Singularity should have a custom JEI info panel which explains that it's easily obtainable by pumping water into a Matter Condenser.")
                .define("singularity_desc", true);
            builder.pop();
        }
    }
}
