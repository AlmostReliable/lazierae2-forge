package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.maintainer.MaintainerMenu;
import com.almostreliable.lazierae2.gui.MaintainerScreen;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket;
import com.almostreliable.lazierae2.network.packets.MaintainerSyncPacket.SYNC_FLAGS;
import net.minecraft.client.Minecraft;

final class ClientHandler {

    private ClientHandler() {}

    static <T> void handlePacket(T packet) {
        if (packet instanceof MaintainerSyncPacket) handleMaintainerSyncPacket((MaintainerSyncPacket) packet);
    }

    private static void handleMaintainerSyncPacket(MaintainerSyncPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        var menu = player.containerMenu;
        if (!(menu instanceof MaintainerMenu maintainerMenu)) return;
        var maintainer = maintainerMenu.entity;
        if ((packet.getFlags() & SYNC_FLAGS.STATE) != 0) {
            maintainer.craftRequests.updateState(packet.getSlot(), packet.getState());
        }
        if ((packet.getFlags() & SYNC_FLAGS.STACK) != 0) {
            maintainer.craftRequests.updateStackClient(packet.getSlot(), packet.getStack());
        }
        var screen = Minecraft.getInstance().screen;
        if (!(screen instanceof MaintainerScreen maintainerScreen)) return;
        if ((packet.getFlags() & SYNC_FLAGS.COUNT) != 0) {
            maintainer.craftRequests.updateCount(packet.getSlot(), packet.getCount());
            maintainerScreen.updateCountBox(packet.getSlot(), packet.getCount());
        }
        if ((packet.getFlags() & SYNC_FLAGS.BATCH) != 0) {
            maintainer.craftRequests.updateBatch(packet.getSlot(), packet.getBatch());
            maintainerScreen.updateBatchBox(packet.getSlot(), packet.getBatch());
        }
    }
}
