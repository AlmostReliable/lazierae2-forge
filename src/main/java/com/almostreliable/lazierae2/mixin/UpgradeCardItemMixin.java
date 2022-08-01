package com.almostreliable.lazierae2.mixin;

import appeng.items.materials.UpgradeCardItem;
import appeng.util.InteractionUtil;
import com.almostreliable.lazierae2.content.processor.ProcessorEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UpgradeCardItem.class)
public class UpgradeCardItemMixin {
    @Inject(method = "onItemUseFirst", at = @At("HEAD"), remap = false)
    private void lazierae2$onItemUseFirst(
        ItemStack stack, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir
    ) {
        var player = context.getPlayer();
        var hand = context.getHand();
        if (player != null && InteractionUtil.isInAlternateUseMode(player)) {
            var blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
            if (blockEntity instanceof ProcessorEntity processor) {
                processor.insertUpgrades(player, hand);
            }
        }
    }
}
