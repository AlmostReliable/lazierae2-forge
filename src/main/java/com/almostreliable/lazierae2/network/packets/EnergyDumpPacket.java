package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.content.processor.ProcessorMenu;
import com.almostreliable.lazierae2.network.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

@SuppressWarnings({"java:S1118", "java:S1172"})
public final class EnergyDumpPacket extends ClientToServerPacket<EnergyDumpPacket> {

    @Override
    public void encode(EnergyDumpPacket message, FriendlyByteBuf buffer) {
        // ignored
    }

    @Override
    public EnergyDumpPacket decode(FriendlyByteBuf ignoredBuffer) {
        return new EnergyDumpPacket();
    }

    @Override
    protected void handlePacket(EnergyDumpPacket packet, @Nullable ServerPlayer player) {
        if (player != null && player.containerMenu instanceof ProcessorMenu container) {
            var level = container.entity.getLevel();
            if (level == null || !level.isLoaded(container.entity.getBlockPos())) return;
            container.setEnergyStored(0);
        }
    }
}
