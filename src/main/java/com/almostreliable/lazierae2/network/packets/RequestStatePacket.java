package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.maintainer.MaintainerEntity;
import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class RequestStatePacket extends ClientToServerPacket<RequestStatePacket> {

    private int slot;
    private boolean value;

    public RequestStatePacket(int slot, boolean value) {
        this.slot = slot;
        this.value = value;
    }

    public RequestStatePacket() {}

    @Override
    public void encode(RequestStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeBoolean(packet.value);
    }

    @Override
    public RequestStatePacket decode(FriendlyByteBuf buffer) {
        var packet = new RequestStatePacket();
        packet.slot = buffer.readInt();
        packet.value = buffer.readBoolean();
        return packet;
    }

    @Override
    protected void handlePacket(RequestStatePacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MaintainerMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MaintainerEntity maintainer)) return;
            var level = maintainer.getLevel();
            if (level == null || !level.isLoaded(maintainer.getBlockPos())) return;
            maintainer.craftRequests.get(packet.slot).updateState(packet.value);
        }
    }
}
