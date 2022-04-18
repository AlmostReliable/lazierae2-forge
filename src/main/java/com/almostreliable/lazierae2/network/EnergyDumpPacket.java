package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings({"java:S1118", "java:S1172"})
public final class EnergyDumpPacket {

    static EnergyDumpPacket decode(FriendlyByteBuf ignoredBuffer) {
        return new EnergyDumpPacket();
    }

    static void handle(EnergyDumpPacket ignoredPacket, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(@Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof ProcessorMenu container) {
            var level = container.entity.getLevel();
            if (level == null || !level.isLoaded(container.entity.getBlockPos())) return;
            container.setEnergyStored(0);
        }
    }

    void encode(FriendlyByteBuf ignoredBuffer) {
        // fake encode
    }
}
