package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.requester.RequesterMenu;
import com.almostreliable.lazierae2.inventory.FakeSlot;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class RequestStackPacket extends ClientToServerPacket<RequestStackPacket> {

    private int slot;
    private ItemStack value;

    public RequestStackPacket(int slot, ItemStack value) {
        this.slot = slot;
        this.value = value;
    }

    public RequestStackPacket() {}

    @Override
    public void encode(RequestStackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeItemStack(packet.value, false);
    }

    @Override
    public RequestStackPacket decode(FriendlyByteBuf buffer) {
        var packet = new RequestStackPacket();
        packet.slot = buffer.readInt();
        packet.value = buffer.readItem();
        return packet;
    }

    @Override
    protected void handlePacket(RequestStackPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof RequesterMenu requester) {
            if (!(requester.slots.get(packet.slot) instanceof FakeSlot fakeSlot)) return;
            fakeSlot.set(packet.value);
        }
    }
}
