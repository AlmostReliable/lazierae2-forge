package com.almostreliable.lazierae2.network.sync;

import net.minecraft.network.FriendlyByteBuf;

public interface IMenuSyncable {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);

    boolean hasChanged(Object oldValue);
}
