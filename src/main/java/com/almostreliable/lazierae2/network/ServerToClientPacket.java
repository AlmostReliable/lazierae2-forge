package com.almostreliable.lazierae2.network;

import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;

public abstract class ServerToClientPacket<T> implements IPacket<T> {

    @Override
    public void handle(T packet, Supplier<? extends Context> context) {
        context.get().enqueueWork(() -> handlePacket(packet));
        context.get().setPacketHandled(true);
    }

    private void handlePacket(T packet) {
        ClientHandler.handlePacket(packet);
    }
}
