/*
 * Copyright (C) 2012 MineStar.de 
 * 
 * This file is part of Contao2.
 * 
 * Contao2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * Contao2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.bungeebridge.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.minestar.bungeebridge.manager.StatisticManager;
import de.minestar.bungeebridge.statistics.Statistic;

public class StatisticListener implements Listener {

    private StatisticManager statisticManager;

    public StatisticListener(StatisticManager sManager) {
        this.statisticManager = sManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        String playerName = event.getPlayer().getName();
        Statistic thisStatistic = statisticManager.getPlayersStatistic(playerName);
        thisStatistic.incrementBreak();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        String playerName = event.getPlayer().getName();
        Statistic stat = statisticManager.getPlayersStatistic(playerName);
        if (stat != null)
            stat.incrementPlace();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // load statistics
        if (!this.statisticManager.hasPlayer(event.getPlayer().getName())) {
            System.out.println("loading statistics: " + event.getPlayer().getName());
            this.statisticManager.loadSingleStatistics(event.getPlayer().getName());
            this.statisticManager.loadSingleWarnings(event.getPlayer().getName());
        } else {
            System.out.println("Player already has statistics: " + event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // update statistics
        System.out.println("saving statistics: " + event.getPlayer().getName());
        this.statisticManager.saveStatistic(event.getPlayer().getName());
        this.statisticManager.removePlayer(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        // update statistics
        System.out.println("saving statistics: " + event.getPlayer().getName());
        this.statisticManager.saveStatistic(event.getPlayer().getName());
        this.statisticManager.removePlayer(event.getPlayer().getName());
    }

}
