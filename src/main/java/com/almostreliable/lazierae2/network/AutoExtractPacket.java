package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.machine.MachineEntity;
import com.almostreliable.lazierae2.content.machine.MachineMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AutoExtractPacket {

    private boolean value;

    public AutoExtractPacket(boolean value) {
        this.value = value;
    }

    private AutoExtractPacket() {}

    static AutoExtractPacket decode(FriendlyByteBuf buffer) {
        var packet = new AutoExtractPacket();
        packet.value = buffer.readBoolean();
        return packet;
    }

    static void handle(AutoExtractPacket packet, Supplier<? extends Context> context) {
        var player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(AutoExtractPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MachineMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MachineEntity machine)) return;
            var level = machine.getLevel();
            if (level == null || !level.isLoaded(machine.getBlockPos())) return;
            machine.setAutoExtract(packet.value);
            machine.setChanged();
        }
    }

    void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(value);
    }
}
