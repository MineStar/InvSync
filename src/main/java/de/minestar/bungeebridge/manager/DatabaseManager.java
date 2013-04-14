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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.minestar.bungeebridge.core.BungeeBridgeCore;
import de.minestar.bungeebridge.statistics.MCWarning;
import de.minestar.bungeebridge.statistics.PlayerWarnings;
import de.minestar.bungeebridge.statistics.Statistic;
import de.minestar.minestarlibrary.database.AbstractMySQLHandler;
import de.minestar.minestarlibrary.utils.ConsoleUtils;

public class DatabaseManager extends AbstractMySQLHandler {

    private StatisticManager statisticManager;

    private PreparedStatement addWarning;
    private PreparedStatement deleteWarning;

    private PreparedStatement selectSingleStatistic;
    private PreparedStatement selectSingleWarnings;
    private PreparedStatement saveSingleStatistic;

    public DatabaseManager(String NAME, File SQLConfigFile) {
        super(NAME, SQLConfigFile);
    }

    @Override
    protected void createStructure(String NAME, Connection con) throws Exception {
        // Do nothing - structure is given
    }

    @Override
    protected void createStatements(String NAME, Connection con) throws Exception {

        addWarning = con.prepareStatement("INSERT INTO mc_warning (mc_pay_id,reason,date,adminnickname) VALUES ((SELECT id FROM mc_pay WHERE minecraft_nick = ?), ?, STR_TO_DATE(?,'%d.%m.%Y %H:%i:%s'), ?)");

        selectSingleWarnings = con.prepareStatement("SELECT minecraft_nick, mc_warning.reason, DATE_FORMAT(date, '%d.%m.%Y %H:%i:%s'),adminnickname FROM mc_warning,mc_pay WHERE mc_warning.mc_pay_id = mc_pay.id AND minecraft_nick = ? ORDER BY minecraft_nick,mc_warning.date");

        deleteWarning = con.prepareStatement("DELETE FROM mc_warning WHERE mc_pay_id = (SELECT id FROM mc_pay WHERE minecraft_nick = ?) AND DATE_FORMAT(date,'%d.%m.%Y %H:%i:%s') = ?");

        selectSingleStatistic = con.prepareStatement("SELECT minecraft_nick, totalBreak, totalPlaced FROM mc_pay WHERE minecraft_nick = ?");

        saveSingleStatistic = con.prepareStatement("UPDATE mc_pay SET totalPlaced = totalPlaced + ?, totalBreak = totalBreak + ? WHERE minecraft_nick = ?");
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public boolean addWarning(String playerName, String reason, String adminName) {

        String date = dateFormat.format(new Date());
        statisticManager.addWarning(playerName, new MCWarning(reason, date, adminName));

        // INSERT INTO mc_warning (mc_pay_id,reason,date,adminnickname) VALUES
        // ((SELECT id
        // FROM mc_pay WHERE minecraft_nick = ?), ?, NOW(), ?)
        try {
            addWarning.setString(1, playerName);
            addWarning.setString(2, reason);
            addWarning.setString(3, date);
            addWarning.setString(4, adminName);
            return addWarning.executeUpdate() == 1;
        } catch (Exception e) {
            ConsoleUtils.printException(e, BungeeBridgeCore.NAME, "Can't add a warning to a player! PlayerName=" + playerName + ",adminName=" + adminName + ",text=" + reason);
        }

        return false;
    }

    public boolean removeWarning(String playerName, String date) {
        try {
            deleteWarning.setString(1, playerName);
            deleteWarning.setString(2, date);
            return deleteWarning.executeUpdate() == 1;
        } catch (Exception e) {
            ConsoleUtils.printException(e, BungeeBridgeCore.NAME, "Can't remove a warning from mc_pay! PlayerName=" + playerName + ",WarningDate=" + date);
        }
        return false;
    }

    public Statistic loadSingleStatistic(String playerName) {
        try {
            selectSingleStatistic.setString(1, playerName);
            ResultSet result = selectSingleStatistic.executeQuery();
            while (result.next()) {
                return new Statistic(result.getInt("totalPlaced"), result.getInt("totalBreak"));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlayerWarnings loadSingleWarnings(String playerName) {
        try {
            selectSingleWarnings.setString(1, playerName);
            ResultSet result = selectSingleWarnings.executeQuery();
            PlayerWarnings thisPlayer = new PlayerWarnings();
            while (result.next()) {
                thisPlayer.addWarning(new MCWarning(result.getString(2), result.getString(3), result.getString(4)));
            }
            return thisPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayerWarnings();
        }
    }

    public void updateSingleStatistic(String playerName, int totalPlaced, int totalBreak) {
        try {
            saveSingleStatistic.setInt(1, totalPlaced);
            saveSingleStatistic.setInt(2, totalBreak);
            saveSingleStatistic.setString(3, playerName);
            saveSingleStatistic.executeUpdate();
        } catch (Exception e) {
            ConsoleUtils.printException(e, BungeeBridgeCore.NAME, "Can't store statistics to database! PlayerName=" + playerName + ",totalPlaced=" + totalPlaced + ",totalBreak=" + totalBreak);
        }
    }

    public void initManager(StatisticManager statisticManager) {
        this.statisticManager = statisticManager;
    }

    /**
     * @return the sManager
     */
    public StatisticManager getStatisticManager() {
        return statisticManager;
    }
}