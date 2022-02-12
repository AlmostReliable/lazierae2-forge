package com.github.almostreliable.lazierae2.network;

import com.github.almostreliable.lazierae2.util.TextUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static com.github.almostreliable.lazierae2.core.Constants.NETWORK_ID;

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
        int id = -1;

        CHANNEL
            .messageBuilder(AutoExtractPacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
            .decoder(AutoExtractPacket::decode)
            .encoder(AutoExtractPacket::encode)
            .consumer(AutoExtractPacket::handle)
            .add();
    }
}
