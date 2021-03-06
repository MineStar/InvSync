package de.minestar.bungeebridge.data;

import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.plugin.Plugin;

import de.minestar.minestarlibrary.protocol.BukkitPacketHandler;
import de.minestar.minestarlibrary.protocol.NetworkPacket;
import de.minestar.minestarlibrary.protocol.PacketType;
import de.minestar.minestarlibrary.protocol.packets.DataOKPacket;
import de.minestar.minestarlibrary.protocol.packets.DataRequestPacket;
import de.minestar.minestarlibrary.protocol.packets.DataSendPacket;
import de.minestar.minestarlibrary.protocol.packets.ServerchangeDenyPacket;
import de.minestar.minestarlibrary.protocol.packets.ServerchangeOKPacket;
import de.minestar.minestarlibrary.protocol.packets.ServerchangeRequestPacket;

public class DataPacketHandler extends BukkitPacketHandler {

    public DataPacketHandler(Plugin plugin, String channel) {
        super(plugin, channel);
    }

    @Override
    protected NetworkPacket handlePacket(PacketType packetType, DataInputStream dataInputStream) throws IOException {
        switch (packetType) {
            case SERVERCHANGE_REQUEST : {
                return new ServerchangeRequestPacket(dataInputStream);
            }
            case SERVERCHANGE_OK : {
                return new ServerchangeOKPacket(dataInputStream);
            }
            case SERVERCHANGE_DENY : {
                return new ServerchangeDenyPacket(dataInputStream);
            }
            case DATA_REQUEST : {
                return new DataRequestPacket(dataInputStream);
            }
            case DATA_SEND : {
                return new DataSendPacket(dataInputStream);
            }
            case DATA_OK : {
                return new DataOKPacket(dataInputStream);
            }
            default : {
                return null;
            }
        }
    }

}
