package com.almostreliable.lazierae.blockentity;

public enum MachineState {
    OK, // everything is fine
    NO_BLOCK, // no block found above the injector
    NO_ITEM, // no item found in the input slot
    NO_RECIPE, // no recipe found for the current ingredients
    NO_ENERGY // not enough energy to process the current recipe
}
