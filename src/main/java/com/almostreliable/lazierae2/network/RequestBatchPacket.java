package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RequestBatchPacket {

    private int slot;
    private long value;

    public RequestBatchPacket(int slot, long value) {
        this.slot = slot;
        this.value = value;
    }

    private RequestBatchPacket() {}

    static RequestBatchPacket decode(FriendlyByteBuf buffer) {
        var packet = new RequestBatchPacket();
        packet.slot = buffer.readInt();
        packet.value = buffer.readLong();
        return packet;
    }

    static void handle(RequestBatchPacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(RequestBatchPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MaintainerMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MaintainerEntity maintainer)) return;
            var level = maintainer.getLevel();
            if (level == null || !level.isLoaded(maintainer.getBlockPos())) return;
            maintainer.craftRequests.updateBatch(packet.slot, packet.value);
            maintainer.syncClient();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeLong(value);
    }
}
