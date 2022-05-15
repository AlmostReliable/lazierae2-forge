package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.network.ServerToClientPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class MenuSyncPacket extends ServerToClientPacket<MenuSyncPacket> {

    private int menuId;
    private FriendlyByteBuf data;

    public MenuSyncPacket(
        int menuId, Consumer<? super FriendlyByteBuf> writer
    ) {
        this.menuId = menuId;
        data = new FriendlyByteBuf(Unpooled.buffer());
        writer.accept(data);
    }

    public MenuSyncPacket() {}

    @Override
    public void encode(MenuSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.menuId);
        buffer.writeInt(packet.data.readableBytes());
        buffer.writeBytes(packet.data, packet.data.readableBytes());
    }

    @Override
    public MenuSyncPacket decode(FriendlyByteBuf buffer) {
        var packet = new MenuSyncPacket();
        packet.menuId = buffer.readInt();
        var length = buffer.readInt();
        packet.data = new FriendlyByteBuf(buffer.readBytes(length));
        return packet;
    }

    public int getMenuId() {
        return menuId;
    }

    public FriendlyByteBuf getData() {
        return data;
    }
}
