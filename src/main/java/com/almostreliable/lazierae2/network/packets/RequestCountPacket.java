package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
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
        buffer.writeInt(slot);
        buffer.writeLong(value);
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
        if (player != null && player.containerMenu instanceof MaintainerMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MaintainerEntity maintainer)) return;
            var level = maintainer.getLevel();
            if (level == null || !level.isLoaded(maintainer.getBlockPos())) return;
            maintainer.craftRequests.updateCount(packet.slot, packet.value);
            maintainer.syncClient();
        }
    }
}
