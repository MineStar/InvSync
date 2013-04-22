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

package de.minestar.bungeebridge.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.core.RequestThread;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.minestarlibrary.protocol.packets.ChatDeathPacket;
import de.minestar.minestarlibrary.protocol.packets.ChatMessagePacket;
import de.minestar.minestarlibrary.protocol.packets.DataRequestPacket;

public class ActionListener implements Listener {

    public static ActionListener INSTANCE;
    private DataPacketHandler dataPacketHandler;

    public ActionListener(DataPacketHandler dataPacketHandler) {
        ActionListener.INSTANCE = this;
        this.dataPacketHandler = dataPacketHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // remove join-message
        event.setJoinMessage(null);

        if (BungeeBridgeCore.SYNC_DATA) {
            // send DataRequestPacket
            DataRequestPacket packet = new DataRequestPacket(event.getPlayer().getName());
            Bukkit.getScheduler().runTaskLater(BungeeBridgeCore.INSTANCE, new RequestThread(this.dataPacketHandler, event.getPlayer(), packet), 2L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // remove quit-message
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        event.getPlayer().sendPluginMessage(BungeeBridgeCore.INSTANCE, "ftb-test", new byte[512]);

        if (!BungeeBridgeCore.SYNC_CHAT) {
            return;
        }

        // send ChatMessagePacket
        ChatMessagePacket packet = new ChatMessagePacket("BUKKIT", event.getMessage());
        this.dataPacketHandler.send(packet, event.getPlayer(), this.dataPacketHandler.getChannel());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        // remove kick-message
        event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!BungeeBridgeCore.SYNC_DEATH) {
            return;
        }

        if (event.getDeathMessage() == null && event.getDeathMessage().length() < 1) {
            return;
        }

        // send ChatDeathPacket
        ChatDeathPacket packet = new ChatDeathPacket("BUKKIT", event.getDeathMessage());
        this.dataPacketHandler.send(packet, event.getEntity(), this.dataPacketHandler.getChannel());
    }

}
