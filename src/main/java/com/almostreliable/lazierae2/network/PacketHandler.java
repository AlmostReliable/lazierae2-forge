package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.almostreliable.lazierae2.core.Constants.NETWORK_ID;

public final class PacketHandler {

    private static final ResourceLocation ID = TextUtil.getRL(NETWORK_ID);
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ID,
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private PacketHandler() {}

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    public static void init() {
        var id = -1;

        CHANNEL
            .messageBuilder(AutoExtractPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(AutoExtractPacket::decode)
            .encoder(AutoExtractPacket::encode)
            .consumer(AutoExtractPacket::handle)
            .add();
        CHANNEL
            .messageBuilder(EnergyDumpPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(EnergyDumpPacket::decode)
            .encoder(EnergyDumpPacket::encode)
            .consumer(EnergyDumpPacket::handle)
            .add();
        CHANNEL
            .messageBuilder(SideConfigPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(SideConfigPacket::decode)
            .encoder(SideConfigPacket::encode)
            .consumer(SideConfigPacket::handle)
            .add();
        CHANNEL
            .messageBuilder(RequestCountPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestCountPacket::decode)
            .encoder(RequestCountPacket::encode)
            .consumer(RequestCountPacket::handle)
            .add();
        CHANNEL
            .messageBuilder(RequestBatchPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestBatchPacket::decode)
            .encoder(RequestBatchPacket::encode)
            .consumer(RequestBatchPacket::handle)
            .add();
        CHANNEL
            .messageBuilder(RequestStatePacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(RequestStatePacket::decode)
            .encoder(RequestStatePacket::encode)
            .consumer(RequestStatePacket::handle)
            .add();
    }
}
