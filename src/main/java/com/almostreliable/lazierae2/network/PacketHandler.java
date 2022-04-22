package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.network.packets.*;
import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.almostreliable.lazierae2.core.Constants.NETWORK_ID;

public final class PacketHandler {

    private static final ResourceLocation ID = TextUtil.getRL(NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(ID)
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(PROTOCOL::equals)
        .serverAcceptedVersions(PROTOCOL::equals)
        .simpleChannel();

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init() {
        var packetId = -1;
        // client to server
        register(++packetId, AutoExtractPacket.class, new AutoExtractPacket());
        register(++packetId, EnergyDumpPacket.class, new EnergyDumpPacket());
        register(++packetId, RequestBatchPacket.class, new RequestBatchPacket());
        register(++packetId, RequestCountPacket.class, new RequestCountPacket());
        register(++packetId, RequestStatePacket.class, new RequestStatePacket());
        register(++packetId, SideConfigPacket.class, new SideConfigPacket());
        // server to client
        register(++packetId, MaintainerSyncPacket.class, new MaintainerSyncPacket());
    }

    private static <T> void register(int packetId, Class<T> clazz, IPacket<T> packet) {
        CHANNEL.registerMessage(packetId, clazz, packet::encode, packet::decode, packet::handle);
    }
}
