package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.GenericDataHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class StackArrayDataHandler extends GenericDataHandler<ItemStack[]> {

    public StackArrayDataHandler(Supplier<ItemStack[]> getter, Consumer<? super ItemStack[]> setter) {
        super(getter, setter);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, ItemStack... value) {
        buffer.writeVarInt(value.length);
        for (var stack : value) {
            buffer.writeItemStack(stack, true);
        }
    }

    @Override
    protected ItemStack[] handleDecoding(FriendlyByteBuf buffer) {
        var length = buffer.readVarInt();
        var array = new ItemStack[length];
        for (var i = 0; i < length; i++) {
            array[i] = buffer.readItem();
        }
        return array;
    }
}
