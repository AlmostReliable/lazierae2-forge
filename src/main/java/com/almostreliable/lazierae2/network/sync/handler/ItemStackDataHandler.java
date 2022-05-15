package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ItemStackDataHandler extends GenericDataHandler<ItemStack> {

    public ItemStackDataHandler(Supplier<ItemStack> getter, Consumer<? super ItemStack> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, ItemStack value) {
        buffer.writeItemStack(value, true);
    }

    @Override
    protected ItemStack handleDecoding(FriendlyByteBuf buffer) {
        return buffer.readItem();
    }
}
