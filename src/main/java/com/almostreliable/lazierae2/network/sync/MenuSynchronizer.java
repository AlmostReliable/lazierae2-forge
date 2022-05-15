package com.almostreliable.lazierae2.network.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MenuSynchronizer {

    private final Map<Short, DataHandler<?>> dataHandlers = new HashMap<>();

    public void encode(FriendlyByteBuf buffer) {
        handleEncode(buffer, false);
    }

    public void encodeAll(FriendlyByteBuf buffer) {
        handleEncode(buffer, true);
    }

    public void decode(FriendlyByteBuf buffer) {
        for (var id = buffer.readShort(); id != -1; id = buffer.readShort()) {
            var dataHandler = dataHandlers.get(id);
            if (dataHandler == null) {
                LogUtils.getLogger().warn("Unknown data handler with ID {} in {}!", id, buffer.getClass().getName());
                continue;
            }
            dataHandler.decode(buffer);
        }
    }

    public boolean hasDataHandlers() {
        return !dataHandlers.isEmpty();
    }

    public boolean hasChanged() {
        for (var dataHandler : dataHandlers.values()) {
            if (dataHandler.hasChanged()) return true;
        }
        return false;
    }

    public <T> void addDataHandler(Class<T> clazz, Supplier<T> getter, Consumer<? super T> setter) {
        var dataHandler = DataHandler.create(clazz, getter, setter);
        dataHandlers.put((short) dataHandlers.size(), dataHandler);
    }

    private void handleEncode(FriendlyByteBuf buffer, boolean all) {
        for (var dataHandler : dataHandlers.entrySet()) {
            if (all || dataHandler.getValue().hasChanged()) {
                buffer.writeShort(dataHandler.getKey());
                dataHandler.getValue().encode(buffer);
            }
        }
        buffer.writeVarInt(-1);
    }
}
