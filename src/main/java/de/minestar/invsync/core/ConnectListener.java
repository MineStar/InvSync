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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.minestar.protocol.newpackets.PacketHandler;
import de.minestar.protocol.newpackets.packets.InventoryRequestPackage;

public class ConnectListener implements Listener {

    public static ConnectListener INSTANCE;
    public DataHandler dataHandler;

    public ConnectListener(DataHandler dataHandler) {
        ConnectListener.INSTANCE = this;
        this.dataHandler = dataHandler;
    }

    public void saveData(Player player) {
        // send inventory to bungee
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void test(PlayerDropItemEvent event) {
        InventoryRequestPackage packet = new InventoryRequestPackage(event.getPlayer().getName());
        PacketHandler.INSTANCE.send(packet, event.getPlayer(), PacketHandler.CHANNEL);
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
