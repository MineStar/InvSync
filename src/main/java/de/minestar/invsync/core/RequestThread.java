package de.minestar.invsync.core;

import org.bukkit.entity.Player;

import de.minestar.protocol.newpackets.NetworkPacket;
import de.minestar.protocol.newpackets.PacketHandler;

public class RequestThread implements Runnable {

    private Player player;
    private NetworkPacket packet;

    public RequestThread(Player player, NetworkPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public void run() {
        PacketHandler.INSTANCE.send(this.packet, this.player, PacketHandler.CHANNEL);
    }

}
