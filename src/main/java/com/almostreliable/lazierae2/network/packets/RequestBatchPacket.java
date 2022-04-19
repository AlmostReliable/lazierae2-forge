package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
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
        buffer.writeInt(slot);
        buffer.writeLong(value);
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
        if (player != null && player.containerMenu instanceof MaintainerMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MaintainerEntity maintainer)) return;
            var level = maintainer.getLevel();
            if (level == null || !level.isLoaded(maintainer.getBlockPos())) return;
            maintainer.craftRequests.updateBatch(packet.slot, packet.value);
            maintainer.syncClient();
        }
    }
}
