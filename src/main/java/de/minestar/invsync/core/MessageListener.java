package de.minestar.invsync.core;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import de.minestar.protocol.newpackets.NetworkPacket;
import de.minestar.protocol.newpackets.PacketHandler;
import de.minestar.protocol.newpackets.packets.InventoryRequestPackage;

public class MessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        // System.out.println("received package... " + data.length);
        // Packet packet = Packet.readPacket(data);
        // if (packet != null) {
        // // decode answer
        // System.out.println("valid packet received!");
        // String message = new String(packet.getData(), Packet.UTF8);
        // System.out.println("message was: " + message);
        // }

        // get packet
        NetworkPacket packet = PacketHandler.INSTANCE.extractPacket(data);
        if (packet != null) {
            switch (packet.getType()) {
                case INVENTORY_REQUEST : {
                    this.handleInventoryRequest((InventoryRequestPackage) packet);
                    break;
                }
                default : {
                    break;
                }
            }
        }
    }

    private void handleInventoryRequest(InventoryRequestPackage packet) {
        System.out.println("INVENTORY_REQUEST from player: " + packet.getPlayerName());
    }
}
