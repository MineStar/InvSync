package de.minestar.protocol.newpackets.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.minestar.protocol.newpackets.NetworkPacket;
import de.minestar.protocol.newpackets.PacketType;

public class InventoryDataPacket extends NetworkPacket {

    private String playerName;
    private int datalength;
    private byte[] data;

    public InventoryDataPacket(String playerName, byte[] data) {
        super(PacketType.INVENTORY_DATA);
        this.playerName = playerName;
        this.datalength = data.length;
        this.data = data;
    }

    public InventoryDataPacket(PacketType type) {
        super(PacketType.INVENTORY_DATA);
    }

    public InventoryDataPacket(DataInputStream dataInputStream) throws IOException {
        super(PacketType.INVENTORY_DATA, dataInputStream);
    }

    public PacketType getPacketType() {
        return PacketType.INVENTORY_DATA;
    }

    public String getPlayerName() {
        return playerName;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public void onSend(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(this.playerName);
        dataOutputStream.writeInt(this.datalength);
        dataOutputStream.write(this.data);
    }

    @Override
    public void onReceive(DataInputStream dataInputStream) throws IOException {
        this.playerName = dataInputStream.readUTF();
        this.datalength = dataInputStream.readInt();
        this.data = new byte[this.datalength];
        dataInputStream.read(this.data);
    }
}
