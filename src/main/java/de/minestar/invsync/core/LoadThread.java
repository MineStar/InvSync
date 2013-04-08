/*
 * Copyright (C) 2013 MineStar.de 
 * 
 * This file is part of MinestarCore.
 * 
 * MinestarCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MinestarCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MinestarCore.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.invsync.core;

import org.bukkit.entity.Player;

public class LoadThread implements Runnable {
    private Player player;

    public LoadThread(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        ConnectListener.INSTANCE.loadData(player);
    }
}
