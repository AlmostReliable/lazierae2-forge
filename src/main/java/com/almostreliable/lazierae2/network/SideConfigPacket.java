package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.component.SideConfiguration;
import com.almostreliable.lazierae2.machine.MachineContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SideConfigPacket {

    private CompoundTag config;

    public SideConfigPacket(SideConfiguration config) {
        this.config = config.serializeNBT();
    }

    private SideConfigPacket() {}

    static SideConfigPacket decode(FriendlyByteBuf buffer) {
        var packet = new SideConfigPacket();
        packet.config = buffer.readNbt();
        return packet;
    }

    static void handle(SideConfigPacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(SideConfigPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MachineContainer) {
            var entity = ((MachineContainer) player.containerMenu).entity;
            var level = entity.getLevel();
            if (level == null || !level.isLoaded(entity.getBlockPos())) return;
            entity.sideConfig.deserializeNBT(packet.config);
            entity.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(config);
    }
}
