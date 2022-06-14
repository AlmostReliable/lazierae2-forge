package com.almostreliable.lazierae2.network;

import com.almostreliable.lazierae2.content.GenericMenu;
import com.almostreliable.lazierae2.gui.RequesterScreen;
import com.almostreliable.lazierae2.network.packets.MenuSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientHandler {

    private ClientHandler() {}

    public static void updateRequestGui(int slot) {
        var screen = Minecraft.getInstance().screen;
        if (!(screen instanceof RequesterScreen gui)) return;
        gui.requestControl.refreshControlBoxes(slot);
    }

    static <T> void handlePacket(T packet) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (packet instanceof MenuSyncPacket menuPacket) handleMenuSyncPacket(menuPacket, player);
    }

    private static void handleMenuSyncPacket(MenuSyncPacket packet, LocalPlayer player) {
        if (player.containerMenu instanceof GenericMenu menu && packet.getMenuId() == menu.containerId) {
            menu.receiveServerData(packet.getData());
        }
    }
}
