package com.github.almostreliable.lazierae2.network;

import com.github.almostreliable.lazierae2.machine.MachineContainer;
import com.github.almostreliable.lazierae2.machine.MachineTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AutoExtractPacket {

    private boolean value;

    public AutoExtractPacket(boolean value) {
        this.value = value;
    }

    private AutoExtractPacket() {}

    static AutoExtractPacket decode(PacketBuffer buffer) {
        AutoExtractPacket packet = new AutoExtractPacket();
        packet.value = buffer.readBoolean();
        return packet;
    }

    static void handle(AutoExtractPacket packet, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(AutoExtractPacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MachineContainer) {
            MachineTile tile = ((MachineContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.setAutoExtract(packet.value);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeBoolean(value);
    }
}
