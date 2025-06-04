package com.almostreliable.lazierae.mixin;

import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Accessor("slot")
    int getSlot();
}
