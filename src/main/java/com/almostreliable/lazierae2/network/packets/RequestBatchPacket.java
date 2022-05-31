package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.content.requester.RequesterMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class RequestBatchPacket extends ClientToServerPacket<RequestBatchPacket> {

    private int slot;
    private long value;

    public RequestBatchPacket(int slot, long value) {
        this.slot = slot;
        this.value = value;
    }

    public RequestBatchPacket() {}

    @Override
    public void encode(RequestBatchPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeLong(packet.value);
    }

    @Override
    public RequestBatchPacket decode(FriendlyByteBuf buffer) {
        var packet = new RequestBatchPacket();
        packet.slot = buffer.readInt();
        packet.value = buffer.readLong();
        return packet;
    }

    @Override
    protected void handlePacket(RequestBatchPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof RequesterMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof RequesterEntity requester)) return;
            var level = requester.getLevel();
            if (level == null || !level.isLoaded(requester.getBlockPos())) return;
            requester.craftRequests.get(packet.slot).updateBatch(packet.value);
        }
    }
}
