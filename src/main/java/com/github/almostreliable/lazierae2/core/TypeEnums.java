package com.github.almostreliable.lazierae2.core;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Defines the type of the translation to
     * identify its key inside the lang file.
     */
    public enum TRANSLATE_TYPE {
        CONTAINER, LABEL, TOOLTIP, BLOCK_SIDE, IO_SETTING, NUMBER, STATUS, MODE, ACCURACY
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
}
