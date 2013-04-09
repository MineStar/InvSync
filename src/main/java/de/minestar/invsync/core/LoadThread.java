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

import net.minecraft.server.v1_5_R2.NBTTagCompound;

import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;

public class LoadThread implements Runnable {
    private CraftPlayer player;
    private NBTTagCompound tagCompound;

    public LoadThread(CraftPlayer player, NBTTagCompound tagCompound) {
        this.player = player;
        this.tagCompound = tagCompound;
    }

    @Override
    public void run() {
        System.out.println("updating inventory with saved one!");
        DataHandler.INSTANCE.applyInventory(this.player.getHandle(), this.tagCompound);
    }
}
