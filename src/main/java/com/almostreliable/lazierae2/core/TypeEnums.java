package com.almostreliable.lazierae2.core;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Defines the type of the translation to
     * identify its key inside the lang file.
     */
    public enum TRANSLATE_TYPE {
        BLOCK, TOOLTIP, BLOCK_SIDE, IO_SETTING, EXTRACT_SETTING, CONFIG
    }

    /**
     * Defines the possible IO sides of a block.
     */
    public enum BLOCK_SIDE {
        BOTTOM, TOP, FRONT, BACK, LEFT, RIGHT
    }

    /**
     * Enum to represent the different IO settings for the side configuration.
     */
    public enum IO_SETTING {
        OFF, INPUT, OUTPUT, IO
    }

    /**
     * Enum to represent the different states for the auto extraction.
     */
    public enum EXTRACT_SETTING {
        OFF, ON
    }
}
