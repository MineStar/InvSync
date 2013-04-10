/*
 * Copyright (C) 2013 MineStar.de 
 * 
 * This file is part of MineStarLibrary.
 * 
 * MineStarLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MineStarLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MineStarLibrary.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.invsync.core;

import java.io.IOException;

import net.minecraft.server.v1_5_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;
import de.minestar.protocol.newpackets.packets.InventoryDataPacket;
import de.minestar.protocol.newpackets.packets.InventoryRequestPacket;

public class ConnectListener implements Listener {

    public static ConnectListener INSTANCE;
    private InventoryPacketHandler inventoryPacketHandler;
    private DataHandler dataHandler;

    public ConnectListener(InventoryPacketHandler inventoryPacketHandler, DataHandler dataHandler) {
        ConnectListener.INSTANCE = this;
        this.inventoryPacketHandler = inventoryPacketHandler;
        this.dataHandler = dataHandler;
    }

    public void saveData(Player player) {
        try {
            // send inventory to bungee
            System.out.println("SENDING INVENTORY TO BUNGEE");
            CraftPlayer cPlayer = (CraftPlayer) player;
            NBTTagCompound tagCompound = this.dataHandler.getInventoryCompound(cPlayer.getHandle());
            InventoryDataPacket packet = new InventoryDataPacket(player.getName(), CompressedStreamTools.writeMapToByteArray(tagCompound));
            this.inventoryPacketHandler.send(packet, player, this.inventoryPacketHandler.getChannel());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        this.saveData(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        InventoryRequestPacket packet = new InventoryRequestPacket(event.getPlayer().getName());
        Bukkit.getScheduler().runTaskLater(InvSyncCore.INSTANCE, new RequestThread(this.inventoryPacketHandler, event.getPlayer(), packet), 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisconnect(PlayerQuitEvent event) {
        this.saveData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        this.saveData(event.getPlayer());
    }

    public void onShutdown() {
        Player[] playerList = Bukkit.getServer().getOnlinePlayers();
        for (Player player : playerList) {
            this.saveData(player);
        }
    }

}
