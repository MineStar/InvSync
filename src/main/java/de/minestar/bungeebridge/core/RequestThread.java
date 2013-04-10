package de.minestar.bungeebridge.core;

import org.bukkit.entity.Player;

import de.minestar.bungeebridge.protocol.BukkitPacketHandler;
import de.minestar.bungeebridge.protocol.NetworkPacket;

public class RequestThread implements Runnable {

    private BukkitPacketHandler packetHandler;
    private Player player;
    private NetworkPacket packet;

    public RequestThread(BukkitPacketHandler packetHandler, Player player, NetworkPacket packet) {
        this.packetHandler = packetHandler;
        this.player = player;
        this.packet = packet;
    }

    @Override
    public void run() {
        // we need a player
        if (this.player == null) {
            return;
        }

        // send packet
        this.packetHandler.send(this.packet, this.player, this.packetHandler.getChannel());
    }

}
