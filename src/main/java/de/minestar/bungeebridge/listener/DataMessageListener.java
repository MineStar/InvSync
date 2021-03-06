package de.minestar.bungeebridge.listener;

import java.io.IOException;

import net.minecraft.server.v1_5_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.data.DataHandler;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;
import de.minestar.minestarlibrary.protocol.NetworkPacket;
import de.minestar.minestarlibrary.protocol.packets.DataOKPacket;
import de.minestar.minestarlibrary.protocol.packets.DataSendPacket;
import de.minestar.minestarlibrary.protocol.packets.ServerchangeDenyPacket;
import de.minestar.minestarlibrary.protocol.packets.ServerchangeOKPacket;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class DataMessageListener implements PluginMessageListener {

    private DataPacketHandler dataPacketHandler;

    public DataMessageListener(DataPacketHandler dataPacketHandler) {
        this.dataPacketHandler = dataPacketHandler;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        // correct channel
        if (!channel.equalsIgnoreCase(this.dataPacketHandler.getChannel())) {
            return;
        }

        // get packet
        NetworkPacket packet = this.dataPacketHandler.extractPacket(data);
        if (packet != null) {
            switch (packet.getType()) {
                case SERVERCHANGE_OK : {
                    this.handleServerchangeOK(player, (ServerchangeOKPacket) packet);
                    break;
                }
                case SERVERCHANGE_DENY : {
                    this.handleServerchangeDeny(player, (ServerchangeDenyPacket) packet);
                    break;
                }
                case DATA_SEND : {
                    this.handleDataSend(player, (DataSendPacket) packet);
                    break;
                }
                case FREESLOTS_ANSWER : {
                    // TODO: HANDLE FREESLOTS
                    break;
                }
                default : {
                    break;
                }
            }
        } else {
            System.out.println("ERROR: Invalid packet received!");
        }
    }

    private void handleDataSend(Player player, DataSendPacket packet) {
        try {
            player = Bukkit.getPlayerExact(packet.getPlayerName());
            if (player != null && player.isOnline()) {
                if (BungeeBridgeCore.SYNC_DATA) {
                    // apply data
                    NBTTagCompound tagCompound = CompressedStreamTools.loadMapFromByteArray(packet.getData());
                    DataHandler.INSTANCE.applyData(((CraftPlayer) player).getHandle(), tagCompound);
                }

                // send packet
                DataOKPacket dataPacket = new DataOKPacket(packet.getPlayerName());
                this.dataPacketHandler.send(dataPacket, player, this.dataPacketHandler.getChannel());
            }
        } catch (IOException e) {
            PlayerUtils.sendError(player, "Bungee", "Could not load data!");
            e.printStackTrace();
        }
    }

    private void handleServerchangeDeny(Player player, ServerchangeDenyPacket packet) {
        player = Bukkit.getPlayerExact(packet.getPlayerName());
        if (player != null && player.isOnline()) {
            PlayerUtils.sendError(player, "Bungee", packet.getReason());
        }
    }

    private void handleServerchangeOK(Player player, ServerchangeOKPacket packet) {
        try {
            player = Bukkit.getPlayerExact(packet.getPlayerName());
            if (player != null && player.isOnline()) {
                // send info
                PlayerUtils.sendInfo(player, "Bungee", packet.getMessage());

                // create data
                NBTTagCompound tagCompound = DataHandler.INSTANCE.getDataCompound(((CraftPlayer) player).getHandle());

                // send DataSendPacket
                DataSendPacket dataPacket = new DataSendPacket(packet.getPlayerName(), packet.getServerName(), CompressedStreamTools.writeMapToByteArray(tagCompound));
                this.dataPacketHandler.send(dataPacket, player, this.dataPacketHandler.getChannel());
            }
        } catch (IOException e) {
            PlayerUtils.sendError(player, "Bungee", "Could not sync data!");
            e.printStackTrace();
        }
    }
}
