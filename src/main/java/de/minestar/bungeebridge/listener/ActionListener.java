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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.core.RequestThread;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.bungeebridge.protocol.packets.DataRequestPacket;

public class ActionListener implements Listener {

    public static ActionListener INSTANCE;
    private DataPacketHandler inventoryPacketHandler;

    public ActionListener(DataPacketHandler inventoryPacketHandler) {
        ActionListener.INSTANCE = this;
        this.inventoryPacketHandler = inventoryPacketHandler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DataRequestPacket packet = new DataRequestPacket(event.getPlayer().getName());
        Bukkit.getScheduler().runTaskLater(BungeeBridgeCore.INSTANCE, new RequestThread(this.inventoryPacketHandler, event.getPlayer(), packet), 3L);
    }

}
