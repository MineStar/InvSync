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

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectListener implements Listener {

    public static ConnectListener INSTANCE;
    public DataHandler dataHandler;

    private InvSyncCore core;

    public ConnectListener(InvSyncCore core, DataHandler dataHandler) {
        ConnectListener.INSTANCE = this;
        this.core = core;
        this.dataHandler = dataHandler;
    }

    public void saveData(Player player) {
        // send inventory to bungee
        try {
            CraftPlayer cPlayer = (CraftPlayer) player;
            MultiPacket multiPacket = new MultiPacket("Forward", "ALL", PacketType.INVENTORY_SAVE);
            multiPacket.addPacket(Packet.createPackage(PacketType.PLAYERNAME, player.getName()));
            multiPacket.addPacket(Packet.createPackage(PacketType.INVENTORY_SAVE, this.dataHandler.getByteStream(cPlayer.getHandle()).toByteArray()));
            this.sendPackage(player, multiPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendPackage(Player player, MultiPacket packet) {
        try {
            String channelName = "globalchat";
            player.sendPluginMessage(this.core, channelName, packet.getByteOutputStream().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData(Player player) {
        // only load, if there is data present
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisconnect(PlayerQuitEvent event) {
        this.saveData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        this.saveData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConnect(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this.core, new LoadThread(event.getPlayer()), 1l);
    }

    public void onShutdown() {
        Player[] playerList = Bukkit.getServer().getOnlinePlayers();
        for (Player player : playerList) {
            this.saveData(player);
        }
    }

}
