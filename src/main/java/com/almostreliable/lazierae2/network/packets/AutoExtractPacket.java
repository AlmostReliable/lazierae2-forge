package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.content.processor.ProcessorEntity;
import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class AutoExtractPacket extends ClientToServerPacket<AutoExtractPacket> {

    private boolean value;

    public AutoExtractPacket(boolean value) {
        this.value = value;
    }

    public AutoExtractPacket() {}

    @Override
    public void encode(AutoExtractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.value);
    }

    @Override
    public AutoExtractPacket decode(FriendlyByteBuf buffer) {
        var packet = new AutoExtractPacket();
        packet.value = buffer.readBoolean();
        return packet;
    }

    @Override
    protected void handlePacket(AutoExtractPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof ProcessorMenu) {
            var entity = ((GenericMenu<?>) player.containerMenu).entity;
            if (!(entity instanceof ProcessorEntity processor)) return;
            var level = processor.getLevel();
            if (level == null || !level.isLoaded(processor.getBlockPos())) return;
            processor.setAutoExtract(packet.value);
            processor.setChanged();
        }
    }
}
