package com.github.almostreliable.lazierae2.core;

import com.github.almostreliable.lazierae2.core.TypeEnums.TRANSLATE_TYPE;
import com.github.almostreliable.lazierae2.util.TextUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import org.apache.commons.lang3.tuple.Pair;

import static com.github.almostreliable.lazierae2.core.Constants.*;

public final class Config {

    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private Config() {}

    @SuppressWarnings("java:S1192")
    public static final class CommonConfig {

        public final IntValue aggregatorUpgradeSlots;
        public final IntValue centrifugeUpgradeSlots;
        public final IntValue energizerUpgradeSlots;
        public final IntValue etcherUpgradeSlots;

        public final IntValue aggregatorEnergyBuffer;
        public final IntValue centrifugeEnergyBuffer;
        public final IntValue energizerEnergyBuffer;
        public final IntValue etcherEnergyBuffer;

        public final IntValue aggregatorEnergyBufferUpgrade;
        public final IntValue centrifugeEnergyBufferUpgrade;
        public final IntValue energizerEnergyBufferUpgrade;
        public final IntValue etcherEnergyBufferUpgrade;

        public final IntValue aggregatorEnergyUsage;
        public final IntValue centrifugeEnergyUsage;
        public final IntValue energizerEnergyUsage;
        public final IntValue etcherEnergyUsage;

        public final DoubleValue aggregatorEnergyUsageUpgrade;
        public final DoubleValue centrifugeEnergyUsageUpgrade;
        public final DoubleValue energizerEnergyUsageUpgrade;
        public final DoubleValue etcherEnergyUsageUpgrade;

        public final IntValue aggregatorProcessTime;
        public final IntValue centrifugeProcessTime;
        public final IntValue energizerProcessTime;
        public final IntValue etcherProcessTime;

        public final DoubleValue aggregatorProcessTimeUpgrade;
        public final DoubleValue centrifugeProcessTimeUpgrade;
        public final DoubleValue energizerProcessTimeUpgrade;
        public final DoubleValue etcherProcessTimeUpgrade;

        private CommonConfig(Builder builder) {
            builder.comment(
                "#########################################################################################",
                "This section lets you configure the various values of the mod machines.",
                "",
                "If there is an option mentioning 'upgrades', AE2 Acceleration Cards are meant.",
                "#########################################################################################"
            );

            builder.push(TextUtil.translateAsString(TRANSLATE_TYPE.BLOCK, AGGREGATOR_ID));
            aggregatorUpgradeSlots = builder.comment(
                "The number of upgrades the Aggregator can hold.",
                "Upgrades will speed up the machine and increase the energy buffer at the cost of a higher energy consumption (by default)."
            ).translation(machineTranslation(AGGREGATOR_ID, UPGRADE_SLOTS)).defineInRange(UPGRADE_SLOTS, 8, 0, 64);
            aggregatorEnergyBuffer = builder
                .comment(
                    "The amount of energy the Aggregator can hold.",
                    "The energy buffer is used to store energy before it is used."
                )
                .translation(machineTranslation(AGGREGATOR_ID, ENERGY_BUFFER))
                .defineInRange(ENERGY_BUFFER, 100_000, 0, Integer.MAX_VALUE);
            aggregatorEnergyBufferUpgrade = builder
                .comment(
                    "The additional amount of energy the Aggregator can hold per upgrade.",
                    "Each upgrade increases the energy buffer by this amount.",
                    "Setting this to 0 will disable the energy buffer upgrade."
                )
                .translation(machineTranslation(AGGREGATOR_ID, ENERGY_BUFFER_UPGRADE))
                .defineInRange(ENERGY_BUFFER_UPGRADE, 50_000, 0, Integer.MAX_VALUE);
            aggregatorEnergyUsage = builder
                .comment(
                    "The base amount of energy the Aggregator uses per tick.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own energy usage value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(AGGREGATOR_ID, ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, 300, 0, Integer.MAX_VALUE);
            aggregatorEnergyUsageUpgrade = builder
                .comment(
                    "The energy multiplier the Aggregator uses per tick when upgraded.",
                    "Each upgrade multiplies the energy usage by this value.",
                    "Setting this to 1 will disable the energy usage multiplier.",
                    "Lower values than 1 will decrease the energy usage, higher values will increase it.",
                    "The calculation is: energyUsage = energyUsageMultiplier ^ installedUpgrades * baseEnergyUsage"
                )
                .translation(machineTranslation(AGGREGATOR_ID, ENERGY_USAGE_UPGRADE))
                .defineInRange(ENERGY_USAGE_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            aggregatorProcessTime = builder
                .comment(
                    "The base time the Aggregator needs to process a recipe.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own work speed value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(AGGREGATOR_ID, PROCESS_TIME))
                .defineInRange(PROCESS_TIME, 200, 0, Integer.MAX_VALUE);
            aggregatorProcessTimeUpgrade = builder
                .comment(
                    "The process time multiplier the Aggregator uses when upgraded.",
                    "Each upgrade multiplies the process time for a recipe by this value.",
                    "Setting this to 1 will disable the process time multiplier.",
                    "Lower values than 1 will decrease the process time, higher values will increase it.",
                    "The calculation is: processTime = processTimeMultiplier ^ installedUpgrades * baseProcessTime"
                )
                .translation(machineTranslation(AGGREGATOR_ID, PROCESS_TIME_UPGRADE))
                .defineInRange(PROCESS_TIME_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            builder.pop();

            builder.push(TextUtil.translateAsString(TRANSLATE_TYPE.BLOCK, CENTRIFUGE_ID));
            centrifugeUpgradeSlots = builder.comment(
                "The number of upgrades the Centrifuge can hold.",
                "Upgrades will speed up the machine and increase the energy buffer at the cost of a higher energy consumption (by default)."
            ).translation(machineTranslation(CENTRIFUGE_ID, UPGRADE_SLOTS)).defineInRange(UPGRADE_SLOTS, 8, 0, 64);
            centrifugeEnergyBuffer = builder
                .comment(
                    "The amount of energy the Centrifuge can hold.",
                    "The energy buffer is used to store energy before it is used."
                )
                .translation(machineTranslation(CENTRIFUGE_ID, ENERGY_BUFFER))
                .defineInRange(ENERGY_BUFFER, 100_000, 0, Integer.MAX_VALUE);
            centrifugeEnergyBufferUpgrade = builder
                .comment(
                    "The additional amount of energy the Centrifuge can hold per upgrade.",
                    "Each upgrade increases the energy buffer by this amount.",
                    "Setting this to 0 will disable the energy buffer upgrade."
                )
                .translation(machineTranslation(CENTRIFUGE_ID, ENERGY_BUFFER_UPGRADE))
                .defineInRange(ENERGY_BUFFER_UPGRADE, 50_000, 0, Integer.MAX_VALUE);
            centrifugeEnergyUsage = builder
                .comment(
                    "The base amount of energy the Centrifuge uses per tick.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own energy usage value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(CENTRIFUGE_ID, ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, 300, 0, Integer.MAX_VALUE);
            centrifugeEnergyUsageUpgrade = builder
                .comment(
                    "The energy multiplier the Centrifuge uses per tick when upgraded.",
                    "Each upgrade multiplies the energy usage by this value.",
                    "Setting this to 1 will disable the energy usage multiplier.",
                    "Lower values than 1 will decrease the energy usage, higher values will increase it.",
                    "The calculation is: energyUsage = energyUsageMultiplier ^ installedUpgrades * baseEnergyUsage"
                )
                .translation(machineTranslation(CENTRIFUGE_ID, ENERGY_USAGE_UPGRADE))
                .defineInRange(ENERGY_USAGE_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            centrifugeProcessTime = builder
                .comment(
                    "The base time the Centrifuge needs to process a recipe.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own work speed value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(CENTRIFUGE_ID, PROCESS_TIME))
                .defineInRange(PROCESS_TIME, 200, 0, Integer.MAX_VALUE);
            centrifugeProcessTimeUpgrade = builder
                .comment(
                    "The process time multiplier the Centrifuge uses when upgraded.",
                    "Each upgrade multiplies the process time for a recipe by this value.",
                    "Setting this to 1 will disable the process time multiplier.",
                    "Lower values than 1 will decrease the process time, higher values will increase it.",
                    "The calculation is: processTime = processTimeMultiplier ^ installedUpgrades * baseProcessTime"
                )
                .translation(machineTranslation(CENTRIFUGE_ID, PROCESS_TIME_UPGRADE))
                .defineInRange(PROCESS_TIME_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            builder.pop();

            builder.push(TextUtil.translateAsString(TRANSLATE_TYPE.BLOCK, ENERGIZER_ID));
            energizerUpgradeSlots = builder.comment(
                "The amount of upgrade slots the Energizer has.",
                "Each upgrade slot increases the amount of slots by one.",
                "Setting this to 0 will disable the upgrade slots upgrade."
            ).translation(machineTranslation(ENERGIZER_ID, UPGRADE_SLOTS)).defineInRange(UPGRADE_SLOTS, 8, 0, 64);
            energizerEnergyBuffer = builder
                .comment(
                    "The amount of energy the Energizer can hold.",
                    "The energy buffer is used to store energy before it is used."
                )
                .translation(machineTranslation(ENERGIZER_ID, ENERGY_BUFFER))
                .defineInRange(ENERGY_BUFFER, 100_000, 0, Integer.MAX_VALUE);
            energizerEnergyBufferUpgrade = builder
                .comment(
                    "The additional amount of energy the Energizer can hold per upgrade.",
                    "Each upgrade increases the energy buffer by this amount.",
                    "Setting this to 0 will disable the energy buffer upgrade."
                )
                .translation(machineTranslation(ENERGIZER_ID, ENERGY_BUFFER_UPGRADE))
                .defineInRange(ENERGY_BUFFER_UPGRADE, 50_000, 0, Integer.MAX_VALUE);
            energizerEnergyUsage = builder
                .comment(
                    "The base amount of energy the Energizer uses per tick.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own energy usage value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(ENERGIZER_ID, ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, 300, 0, Integer.MAX_VALUE);
            energizerEnergyUsageUpgrade = builder
                .comment(
                    "The energy multiplier the Energizer uses per tick when upgraded.",
                    "Each upgrade multiplies the energy usage by this value.",
                    "Setting this to 1 will disable the energy usage multiplier.",
                    "Lower values than 1 will decrease the energy usage, higher values will increase it.",
                    "The calculation is: energyUsage = energyUsageMultiplier ^ installedUpgrades * baseEnergyUsage"
                )
                .translation(machineTranslation(ENERGIZER_ID, ENERGY_USAGE_UPGRADE))
                .defineInRange(ENERGY_USAGE_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            energizerProcessTime = builder
                .comment(
                    "The base time the Energizer needs to process a recipe.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own work speed value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(ENERGIZER_ID, PROCESS_TIME))
                .defineInRange(PROCESS_TIME, 200, 0, Integer.MAX_VALUE);
            energizerProcessTimeUpgrade = builder
                .comment(
                    "The process time multiplier the Energizer uses when upgraded.",
                    "Each upgrade multiplies the process time for a recipe by this value.",
                    "Setting this to 1 will disable the process time multiplier.",
                    "Lower values than 1 will decrease the process time, higher values will increase it.",
                    "The calculation is: processTime = processTimeMultiplier ^ installedUpgrades * baseProcessTime"
                )
                .translation(machineTranslation(ENERGIZER_ID, PROCESS_TIME_UPGRADE))
                .defineInRange(PROCESS_TIME_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            builder.pop();

            builder.push(TextUtil.translateAsString(TRANSLATE_TYPE.BLOCK, ETCHER_ID));
            etcherUpgradeSlots = builder.comment(
                "The amount of upgrade slots the Etcher has.",
                "Each upgrade slot increases the amount of slots by one.",
                "Setting this to 0 will disable the upgrade slots upgrade."
            ).translation(machineTranslation(ETCHER_ID, UPGRADE_SLOTS)).defineInRange(UPGRADE_SLOTS, 8, 0, 64);
            etcherEnergyBuffer = builder
                .comment(
                    "The amount of energy the Etcher can hold.",
                    "The energy buffer is used to store energy before it is used."
                )
                .translation(machineTranslation(ETCHER_ID, ENERGY_BUFFER))
                .defineInRange(ENERGY_BUFFER, 100_000, 0, Integer.MAX_VALUE);
            etcherEnergyBufferUpgrade = builder
                .comment(
                    "The additional amount of energy the Etcher can hold per upgrade.",
                    "Each upgrade increases the energy buffer by this amount.",
                    "Setting this to 0 will disable the energy buffer upgrade."
                )
                .translation(machineTranslation(ETCHER_ID, ENERGY_BUFFER_UPGRADE))
                .defineInRange(ENERGY_BUFFER_UPGRADE, 50_000, 0, Integer.MAX_VALUE);
            etcherEnergyUsage = builder
                .comment(
                    "The base amount of energy the Etcher uses per tick.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own energy usage value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(ETCHER_ID, ENERGY_USAGE))
                .defineInRange(ENERGY_USAGE, 300, 0, Integer.MAX_VALUE);
            etcherEnergyUsageUpgrade = builder
                .comment(
                    "The energy multiplier the Etcher uses per tick when upgraded.",
                    "Each upgrade multiplies the energy usage by this value.",
                    "Setting this to 1 will disable the energy usage multiplier.",
                    "Lower values than 1 will decrease the energy usage, higher values will increase it.",
                    "The calculation is: energyUsage = energyUsageMultiplier ^ installedUpgrades * baseEnergyUsage"
                )
                .translation(machineTranslation(ETCHER_ID, ENERGY_USAGE_UPGRADE))
                .defineInRange(ENERGY_USAGE_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            etcherProcessTime = builder
                .comment(
                    "The base time the Etcher needs to process a recipe.",
                    "Each recipe can overwrite this value. This is just a fallback value if a recipe does not specify an energy usage.",
                    "All recipes from the mod itself have an own work speed value and this setting won't have any effect on them.",
                    "This is only useful for custom recipes e.g. for modpack makers."
                )
                .translation(machineTranslation(ETCHER_ID, PROCESS_TIME))
                .defineInRange(PROCESS_TIME, 200, 0, Integer.MAX_VALUE);
            etcherProcessTimeUpgrade = builder
                .comment(
                    "The process time multiplier the Etcher uses when upgraded.",
                    "Each upgrade multiplies the process time for a recipe by this value.",
                    "Setting this to 1 will disable the process time multiplier.",
                    "Lower values than 1 will decrease the process time, higher values will increase it.",
                    "The calculation is: processTime = processTimeMultiplier ^ installedUpgrades * baseProcessTime"
                )
                .translation(machineTranslation(ETCHER_ID, PROCESS_TIME_UPGRADE))
                .defineInRange(PROCESS_TIME_UPGRADE, 1.2, 1, Double.MAX_VALUE);
            builder.pop();
        }

        private static String machineTranslation(String machineId, String translationKey) {
            return TextUtil.translateAsString(TRANSLATE_TYPE.CONFIG, machineId + "." + translationKey);
        }
    }
}
