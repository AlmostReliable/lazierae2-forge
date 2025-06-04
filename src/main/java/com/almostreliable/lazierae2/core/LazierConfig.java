package com.almostreliable.lazierae2.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LazierConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    private LazierConfig() {}

    public static final class CommonConfig {

        private CommonConfig(ModConfigSpec.Builder builder) {
            // fill builder
        }
    }
}
