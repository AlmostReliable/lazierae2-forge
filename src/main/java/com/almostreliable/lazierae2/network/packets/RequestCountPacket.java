package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.requester.RequesterEntity;
import com.almostreliable.lazierae2.content.requester.RequesterMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class RequestCountPacket extends ClientToServerPacket<RequestCountPacket> {

    private int slot;
    private long value;

    public RequestCountPacket(int slot, long value) {
        this.slot = slot;
        this.value = value;
    }

    public RequestCountPacket() {}

    @Override
    public void encode(RequestCountPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeLong(packet.value);
    }

    @Override
    public RequestCountPacket decode(FriendlyByteBuf buffer) {
        var packet = new RequestCountPacket();
        packet.slot = buffer.readInt();
        packet.value = buffer.readLong();
        return packet;
    }

    @Override
    protected void handlePacket(RequestCountPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof RequesterMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof RequesterEntity requester)) return;
            var level = requester.getLevel();
            if (level == null || !level.isLoaded(requester.getBlockPos())) return;
            requester.craftRequests.get(packet.slot).updateCount(packet.value);
        }
    }
}
