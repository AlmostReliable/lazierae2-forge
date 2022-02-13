package com.github.almostreliable.lazierae2.network;

import com.github.almostreliable.lazierae2.component.SideConfiguration;
import com.github.almostreliable.lazierae2.machine.MachineContainer;
import com.github.almostreliable.lazierae2.machine.MachineTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SideConfigPacket {

    private CompoundNBT config;

    public SideConfigPacket(SideConfiguration config) {
        this.config = config.serializeNBT();
    }

    private SideConfigPacket() {}

    static SideConfigPacket decode(PacketBuffer buffer) {
        SideConfigPacket packet = new SideConfigPacket();
        packet.config = buffer.readNbt();
        return packet;
    }

    static void handle(SideConfigPacket packet, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(SideConfigPacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MachineContainer) {
            MachineTile tile = ((MachineContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.getSideConfig().deserializeNBT(packet.config);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeNbt(config);
    }
}
