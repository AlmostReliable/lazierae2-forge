package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.machine.MachineContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@SuppressWarnings({"java:S1118", "java:S1172"})
public final class EnergyDumpPacket {

    static EnergyDumpPacket decode(PacketBuffer ignoredBuffer) {
        return new EnergyDumpPacket();
    }

    static void handle(EnergyDumpPacket ignoredPacket, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(@Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MachineContainer) {
            MachineContainer container = (MachineContainer) player.containerMenu;
            World level = container.tile.getLevel();
            if (level == null || !level.isLoaded(container.tile.getBlockPos())) return;
            container.setEnergyStored(0);
        }
    }

    void encode(PacketBuffer ignoredBuffer) {
        // fake encode
    }
}
