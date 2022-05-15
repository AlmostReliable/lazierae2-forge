package com.almostreliable.lazierae2.network.sync.handler;

import com.almostreliable.lazierae2.network.sync.DataHandler;
import com.almostreliable.lazierae2.network.sync.IMenuSyncable;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public final class CustomDataHandler extends DataHandler<IMenuSyncable> {

    public CustomDataHandler(Supplier<? extends IMenuSyncable> getter) {
        super(getter);
    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        handleDecoding(buffer);
    }

    @Override
    protected void handleEncoding(FriendlyByteBuf buffer, IMenuSyncable value) {
        value.encode(buffer);
    }

    @Override
    protected IMenuSyncable handleDecoding(FriendlyByteBuf buffer) {
        var custom = getter.get();
        custom.decode(buffer);
        return custom;
    }

    @Override
    protected boolean hasChanged() {
        return getter.get().hasChanged(oldValue);
    }
}
