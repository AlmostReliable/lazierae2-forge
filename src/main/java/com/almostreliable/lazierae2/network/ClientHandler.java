package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.network.packets.MenuSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

final class ClientHandler {

    private ClientHandler() {}

    static <T> void handlePacket(T packet) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (packet instanceof MenuSyncPacket menuPacket) handleMenuSyncPacket(menuPacket, player);
    }

    private static void handleMenuSyncPacket(MenuSyncPacket packet, Player player) {
        if (player.containerMenu instanceof GenericMenu menu && packet.getMenuId() == menu.containerId) {
            menu.receiveServerData(packet.getData());
        }
    }
}
