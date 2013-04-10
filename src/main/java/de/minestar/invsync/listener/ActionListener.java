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

package de.minestar.invsync.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import de.minestar.invsync.core.InvSyncCore;
import de.minestar.invsync.core.RequestThread;
import de.minestar.invsync.data.DataPacketHandler;
import de.minestar.protocol.newpackets.packets.DataRequestPacket;
import de.minestar.protocol.newpackets.packets.ServerchangeRequestPacket;

public class ActionListener implements Listener {

    public static ActionListener INSTANCE;
    private DataPacketHandler inventoryPacketHandler;

    public ActionListener(DataPacketHandler inventoryPacketHandler) {
        ActionListener.INSTANCE = this;
        this.inventoryPacketHandler = inventoryPacketHandler;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ServerchangeRequestPacket packet = new ServerchangeRequestPacket(event.getPlayer().getName(), "res");
        Bukkit.getScheduler().runTaskLater(InvSyncCore.INSTANCE, new RequestThread(this.inventoryPacketHandler, event.getPlayer(), packet), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DataRequestPacket packet = new DataRequestPacket(event.getPlayer().getName());
        Bukkit.getScheduler().runTaskLater(InvSyncCore.INSTANCE, new RequestThread(this.inventoryPacketHandler, event.getPlayer(), packet), 3L);
    }

}
