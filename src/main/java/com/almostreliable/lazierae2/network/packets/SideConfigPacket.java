package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.component.SideConfiguration;
import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.machine.MachineEntity;
import com.almostreliable.lazierae2.content.machine.MachineMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class SideConfigPacket extends ClientToServerPacket<SideConfigPacket> {

    private CompoundTag config;

    public SideConfigPacket(SideConfiguration config) {
        this.config = config.serializeNBT();
    }

    public SideConfigPacket() {}

    @Override
    public void encode(SideConfigPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(config);
    }

    @Override
    public SideConfigPacket decode(FriendlyByteBuf buffer) {
        var packet = new SideConfigPacket();
        packet.config = buffer.readNbt();
        return packet;
    }

    @Override
    protected void handlePacket(SideConfigPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof MachineMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof MachineEntity machine)) return;
            var level = machine.getLevel();
            if (level == null || !level.isLoaded(machine.getBlockPos())) return;
            machine.sideConfig.deserializeNBT(packet.config);
            machine.setChanged();
        }
    }
}
