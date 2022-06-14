package com.almostreliable.lazierae2.mixin;

import com.almostreliable.lazierae2.inventory.PatternReferenceSlot;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @ModifyVariable(method = "renderSlot", index = 5, at = @At("STORE"))
    private ItemStack lazierae2$changeStackForDisplay(ItemStack stack, PoseStack poseStack, Slot slot) {
        if (slot instanceof PatternReferenceSlot patternSlot) {
            return patternSlot.getDisplayStack();
        }
        return stack;
    }
}
