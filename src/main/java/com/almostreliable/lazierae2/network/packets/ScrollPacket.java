package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.inventory.PatternReferenceSlot;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class ScrollPacket extends ClientToServerPacket<ScrollPacket> {

    private int rowShift;

    public ScrollPacket(int rowShift) {
        this.rowShift = rowShift;
    }

    public ScrollPacket() {}

    @Override
    public void encode(ScrollPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.rowShift);
    }

    @Override
    public ScrollPacket decode(FriendlyByteBuf buffer) {
        return new ScrollPacket(buffer.readInt());
    }

    @Override
    protected void handlePacket(ScrollPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof ProcessorMenu menu) {
            for (var slot : menu.slots) {
                if (slot instanceof PatternReferenceSlot reference) {
                    reference.setRow(packet.rowShift);
                }
            }
        }
    }
}
