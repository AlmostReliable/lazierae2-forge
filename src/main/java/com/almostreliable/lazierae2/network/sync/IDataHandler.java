package com.almostreliable.lazierae2.network.sync;

import net.minecraft.network.FriendlyByteBuf;

public interface IDataHandler {

    void encode(FriendlyByteBuf buffer);

    void decode(FriendlyByteBuf buffer);

    boolean hasChanged();
}
