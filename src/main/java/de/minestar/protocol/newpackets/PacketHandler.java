package de.minestar.protocol.newpackets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.minecraft.server.v1_5_R2.Packet250CustomPayload;

import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.minestar.invsync.core.InvSyncCore;
import de.minestar.protocol.newpackets.packets.InventoryDataPacket;
import de.minestar.protocol.newpackets.packets.InventoryRequestPacket;

public class PacketHandler {

    public static final PacketHandler INSTANCE;
    private static final String BROADCAST = "ALL";
    public static final String CHANNEL = "GlobalChat";

    static {
        INSTANCE = new PacketHandler();
    }

    private static final int MAX_PACKET_SIZE = 32766;

    private final ByteBuffer BUFFER;

    public PacketHandler() {
        BUFFER = ByteBuffer.allocate(MAX_PACKET_SIZE);
    }

    public void send(NetworkPacket packet, Player player, String channel) {
        this.send(packet, player, channel, BungeeSubChannel.FORWARD, BROADCAST);
    }

    public void send(NetworkPacket packet, Player player, String channel, BungeeSubChannel subChannel, String targetServer) {
        if (packet instanceof MultiPacket) {
            MultiPacket multiPacket = (MultiPacket) packet;
            for (NetworkPacket innerPacket : multiPacket) {
                sendPacket(innerPacket, player, channel, subChannel, targetServer);
            }
        } else {
            sendPacket(packet, player, channel, subChannel, targetServer);
        }
    }

    public void send(NetworkPacket packet, Player player, String channel, BungeeSubChannel subChannel) {
        if (packet instanceof MultiPacket) {
            MultiPacket multiPacket = (MultiPacket) packet;
            for (NetworkPacket innerPacket : multiPacket) {
                sendPacket(innerPacket, player, channel, subChannel, null);
            }
        } else {
            sendPacket(packet, player, channel, subChannel, null);
        }
    }

    public final static Charset UFT8 = Charset.forName("UTF-8");

    private void sendPacket(NetworkPacket packet, Player player, String channel, BungeeSubChannel subChannel, String targetServer) {

        // Packet Head:
        //
        // | BUNGEE HEAD |
        // ------------------------------------
        // BungeeChannel (Forward, Connect etc.) - Command to Bungee what to do
        // with the packet
        // TargetServer (ALL or servername) - Receiver of the message
        // ------------------------------------
        // | BUKKIT HEAD |
        // Channel (Own defined plugin channel) - Channel between two plugins
        // DataLength (Length of the data without any head length)
        // Data (Array of bytes - Must be long as defined in DataLength
        //

        // // Create Head
        // BUFFER.clear();
        // // BungeeChannel
        // BUFFER.put(subChannel.getName().getBytes(UFT8));
        // // TargetServer
        // BUFFER.put(targetServer.getBytes(UFT8));
        //
        // // Channel
        // BUFFER.put(channel.getBytes(UFT8));
        //
        // // Placeholder
        // int pos1 = BUFFER.position();
        // BUFFER.putInt(0);
        // int pos2 = BUFFER.position();
        // packet.pack(BUFFER);
        // BUFFER.putInt(pos1, BUFFER.position() - pos2);
        //
        // BUFFER.rewind();
        //
        // // Dirty -.-
        // server.sendData(channel, Arrays.copyOf(BUFFER.array(), BUFFER.limit()));
        // BUFFER.clear();

        try {
            // create streams
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            // BungeeChannel
            dos.writeUTF(subChannel.getName());

            // target
            dos.writeUTF(targetServer);

            // pluginchannel
            dos.writeUTF(channel);

            // pack data
            ByteArrayOutputStream data = packet.pack();
            byte[] dataArray = data.toByteArray();

            // datalength
            dos.writeInt(dataArray.length);

            // data
            dos.write(dataArray);

            // send data
            System.out.println("------------------------------------");
            System.out.println("sending: " + packet.getType());
            this.sendPluginMessage(player, InvSyncCore.INSTANCE, CHANNEL, bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPluginMessage(Player player, Plugin source, String channel, byte[] message) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        if (cPlayer.getHandle().playerConnection == null) {
            System.out.println("no connection");
            return;
        }

        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.tag = channel;
        packet.length = message.length;
        packet.data = message;
        cPlayer.getHandle().playerConnection.sendPacket(packet);
        System.out.println("done!");
    }

    public NetworkPacket extractPacket(byte[] data) {
        try {
            // create streams
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(bis);

            // extract head
            PacketHeadData headData = new PacketHeadData(dataInputStream);

            // get data
            int datalength = dataInputStream.readInt();
            byte[] dataArray = new byte[datalength];
            bis.read(dataArray);

            // create new streams for reading
            bis = new ByteArrayInputStream(dataArray);
            dataInputStream = new DataInputStream(bis);

            // get packettype
            PacketType type = PacketType.get(dataInputStream.readInt());

            switch (type) {
                case INVENTORY_REQUEST : {
                    return new InventoryRequestPacket(dataInputStream);
                }
                case INVENTORY_DATA : {
                    return new InventoryDataPacket(dataInputStream);
                }
                default : {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
    }
}
