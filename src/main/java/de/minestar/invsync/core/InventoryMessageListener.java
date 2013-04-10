package de.minestar.invsync.core;

import net.minecraft.server.v1_5_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;
import de.minestar.protocol.newpackets.NetworkPacket;
import de.minestar.protocol.newpackets.packets.InventoryDataPacket;

public class InventoryMessageListener implements PluginMessageListener {

    private InventoryPacketHandler packetHandler;

    private InventoryMessageListener(InventoryPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        // get packet
        NetworkPacket packet = packetHandler.extractPacket(data);
        if (packet != null) {
            System.out.println("PACKET: " + packet.getType());
            switch (packet.getType()) {
                case INVENTORY_DATA : {
                    this.handleInventoryDataPacket(player, (InventoryDataPacket) packet);
                    break;
                }
                default : {
                    break;
                }
            }
        }
    }

    private void handleInventoryDataPacket(Player player, InventoryDataPacket packet) {
        // names must be equal
        if (!player.getName().equalsIgnoreCase(packet.getPlayerName())) {
            return;
        }

        System.out.println("INVENTORY_DATA received from player: " + packet.getPlayerName());
        try {
            // fetch data
            CraftPlayer cPlayer = (CraftPlayer) player;
            cPlayer.getInventory().clear();
            NBTTagCompound tagCompound = CompressedStreamTools.loadMapFromByteArray(packet.getData());

            // start thread
            Bukkit.getScheduler().runTaskLater(InvSyncCore.INSTANCE, new LoadThread(cPlayer, tagCompound), 5l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
