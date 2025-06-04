package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.BuildConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IDirectionAwarePayloadHandlerBuilder;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import java.util.function.Consumer;

public final class PacketHandler {

    private static final String PROTOCOL = "1";

    private PacketHandler() {}

    public static void registerPackets(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar(BuildConfig.MOD_ID).versioned(PROTOCOL);

        // server to client
//        registerPacket(
//            registrar,
//            RequesterSyncPacket.ID,
//            RequesterSyncPacket::decode,
//            builder -> builder.client(PacketHandler::handlePacket)
//        );

        // client to server
//        registerPacket(
//            registrar,
//            RequestUpdatePacket.ID,
//            RequestUpdatePacket::decode,
//            builder -> builder.server(PacketHandler::handlePacket)
//        );
    }

    private static <P extends Packet> void registerPacket(
        IPayloadRegistrar registrar,
        ResourceLocation id,
        FriendlyByteBuf.Reader<P> decoder,
        Consumer<IDirectionAwarePayloadHandlerBuilder<P, IPlayPayloadHandler<P>>> factory
    ) {
        registrar.play(id, decoder, factory);
    }

    private static <P extends Packet> void handlePacket(P packet, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().execute(() -> packet.handle(player)));
    }
}
