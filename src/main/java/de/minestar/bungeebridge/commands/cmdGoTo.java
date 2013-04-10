/*
 * Copyright (C) 2011 MineStar.de 
 * 
 * This file is part of 'AdminStuff'.
 * 
 * 'AdminStuff' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * 'AdminStuff' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * AUTHOR: GeMoschen
 * 
 */

package de.minestar.bungeebridge.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.core.RequestThread;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.bungeebridge.protocol.packets.ServerchangeRequestPacket;
import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
import de.minestar.minestarlibrary.utils.PlayerUtils;

public class cmdGoTo extends AbstractExtendedCommand {

    private DataPacketHandler dataPacketHandler;

    public cmdGoTo(String syntax, String arguments, String node, DataPacketHandler dataPacketHandler) {
        super("Bungee", syntax, arguments, node);
        this.dataPacketHandler = dataPacketHandler;
    }

    @Override
    /**
     * Representing the command <br>
     * /glue <Player><br>
     * The player is now unable to move
     * 
     * @param player
     *            Called the command
     * @param split
     *            split[0] is the targets name
     */
    public void execute(String[] args, Player player) {
        // check syntax
        if (args.length != 1) {
            PlayerUtils.sendError(player, "Bungee", "Wrong syntax! Use /goto <Name>");
            return;
        }

        // get servername
        String serverName = args[0];
        ServerchangeRequestPacket packet = new ServerchangeRequestPacket(player.getName(), serverName);
        Bukkit.getScheduler().runTaskLater(BungeeBridgeCore.INSTANCE, new RequestThread(this.dataPacketHandler, player, packet), 1L);
    }

}
