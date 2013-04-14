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

package de.minestar.bungeebridge.manager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.statistics.MCWarning;
import de.minestar.bungeebridge.statistics.PlayerWarnings;
import de.minestar.bungeebridge.statistics.Statistic;
import de.minestar.minestarlibrary.utils.ChatUtils;

public class StatisticManager implements Runnable {

    private ConcurrentMap<String, Statistic> statistics = new ConcurrentHashMap<String, Statistic>();
    private ConcurrentMap<String, PlayerWarnings> warnings = new ConcurrentHashMap<String, PlayerWarnings>();
    private DatabaseManager databaseManager;

    public StatisticManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
    }

    public Statistic getPlayersStatistic(String playerName) {
        return statistics.get(playerName);
    }

    public void createSingleStatistics(String playerName) {
        statistics.put(playerName, new Statistic(0, 0));
    }

    public void loadSingleWarnings(String playerName) {
        warnings.put(playerName, databaseManager.loadSingleWarnings(playerName));
    }

    public void saveAllStatistics() {
        for (Map.Entry<String, Statistic> entry : this.statistics.entrySet()) {
            Statistic stats = entry.getValue();
            if (stats != null) {
                if (stats.hasChanged()) {
                    databaseManager.updateSingleStatistic(entry.getKey(), stats.getTotalPlaced(), stats.getTotalBreak());
                    stats.update(0, 0);
                    stats.setHasChanged(false);
                }
            }
        }
        statistics.clear();
    }

    public void saveStatistic(String playerName) {
        Statistic stats = this.getPlayersStatistic(playerName);
        if (stats != null) {
            if (stats.hasChanged()) {
                databaseManager.updateSingleStatistic(playerName, stats.getTotalPlaced(), stats.getTotalBreak());
                stats.update(0, 0);
                stats.setHasChanged(false);
            }
        }
    }

    public boolean hasPlayer(String playerName) {
        return this.statistics.containsKey(playerName);
    }

    public void removePlayer(String playerName) {
        this.statistics.remove(playerName);
        this.warnings.remove(playerName);
    }

    public void initPlayerStatistic(String playerName) {
        if (!this.statistics.containsKey(playerName)) {
            this.statistics.put(playerName, new Statistic(0, 0));
        }
    }

    public PlayerWarnings getWarnings(String playerName) {
        return warnings.get(playerName);
    }

    public void addWarning(String playerName, MCWarning warning) {
        PlayerWarnings thisPlayer = warnings.get(playerName);
        if (thisPlayer == null) {
            thisPlayer = new PlayerWarnings();
            warnings.put(playerName, thisPlayer);
        }
        thisPlayer.addWarning(warning);
    }

    public void printWarnings(Player player) {
        PlayerWarnings thisWarnings = this.getWarnings(player.getName());
        if (thisWarnings != null && thisWarnings.getWarnings().size() > 0) {
            ChatUtils.writeMessage(player, "");
            ChatUtils.writeColoredMessage(player, BungeeBridgeCore.NAME, ChatColor.RED, "Du hast " + thisWarnings.getWarnings().size() + " Verwarnung" + (thisWarnings.getWarnings().size() > 1 ? "en" : "") + "!");
            for (MCWarning warning : thisWarnings.getWarnings()) {
                ChatUtils.writeMessage(player, warning.toString());
            }
        }
    }

    public void printStatistics(Player player) {
        Statistic stats = this.getPlayersStatistic(player.getName());
        ChatUtils.writeMessage(player, "");
        if (stats == null)
            ChatUtils.writeColoredMessage(player, ChatColor.RED, "Du hast keine Statistiken!");
        else {
            ChatUtils.writeColoredMessage(player, ChatColor.BLUE, "Blöcke zerstört: " + stats.getTotalBreak());
            ChatUtils.writeColoredMessage(player, ChatColor.BLUE, "Blöcke gesetzt  : " + stats.getTotalPlaced());
        }
    }

    @Override
    public void run() {
        for (Entry<String, Statistic> entry : statistics.entrySet()) {
            Statistic stats = entry.getValue();
            if (stats.hasChanged()) {
                this.databaseManager.updateSingleStatistic(entry.getKey(), stats.getTotalPlaced(), stats.getTotalBreak());
                stats.setHasChanged(false);
            }
        }
    }
}
