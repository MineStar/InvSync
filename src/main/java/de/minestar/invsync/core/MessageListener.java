package de.minestar.invsync.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class MessageListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        MultiPacket multiPacket = MultiPacket.readPackage(data);
        if (multiPacket != null) {
            // iterate over every packet
            for (Packet packet : multiPacket.getPacketList()) {
                // handle different packettypes
                switch (packet.getPacketType()) {
                    case CHAT : {
                        handleChatPackage(player, packet);
                        break;
                    }
                    case COMMAND : {
                        handleCommandPackage(player, packet);
                        break;
                    }
                    default : {
                        break;
                    }
                }
            }
        }
        
        try {
            byte[] arr = new byte[31766 - 1];
            MultiPacket packet = new MultiPacket("Forward", "ALL", PacketType.MULTIPACKET);
            packet.addPacket(Packet.createPackage(PacketType.JOIN, arr));
            this.sendPackage(player, packet);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendPackage(Player player, MultiPacket packet) {
        try {
            String channelName = "globalchat";
            player.sendPluginMessage(InvSyncCore.INSTANCE, channelName, packet.getByteOutputStream().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleChatPackage(Player player, Packet packet) {
        try {
            String message = new String(packet.getData(), "UTF-8");
            player.sendMessage(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void handleCommandPackage(Player player, Packet packet) {
        try {
            String command = new String(packet.getData(), "UTF-8");
            Bukkit.getServer().dispatchCommand(player, command);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
