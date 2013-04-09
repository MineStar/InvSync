package de.minestar.invsync.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.ItemStack;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagList;
import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;

public class DataHandler {

    public static DataHandler INSTANCE;

    /** Reference to the logger. */
    private static final Logger logger = Logger.getLogger("Minecraft");

    public DataHandler() {
        DataHandler.INSTANCE = this;
    }

    public NBTTagCompound getInventoryCompound(EntityPlayer entityPlayer) {
        // save data
        NBTTagCompound tagCompound = new NBTTagCompound();

        // create taglist
        NBTTagList tagList = new NBTTagList();

        // save inventory
        for (int i = 0; i < entityPlayer.inventory.items.length; i++) {
            if (entityPlayer.inventory.items[i] != null) {
                NBTTagCompound stackCompound = new NBTTagCompound();
                stackCompound.setByte("Slot", (byte) i);
                entityPlayer.inventory.items[i].save(stackCompound);
                tagList.add(stackCompound);
            }
        }

        // save armor
        for (int i = 0; i < entityPlayer.inventory.armor.length; i++) {
            if (entityPlayer.inventory.armor[i] != null) {
                NBTTagCompound stackCompound = new NBTTagCompound();
                stackCompound.setByte("Slot", (byte) (i + 100));
                entityPlayer.inventory.armor[i].save(stackCompound);
                tagList.add(stackCompound);
            }
        }

        // finally set it
        tagCompound.set("Inventory", tagList);
        return tagCompound;
    }

    /**
     * Writes the player data to a DataOutputStream from the specified PlayerEntityMP.
     */
    public ByteArrayOutputStream getByteStream(EntityPlayer entityPlayer) {
        try {
            // save data
            NBTTagCompound tagCompound = this.getInventoryCompound(entityPlayer);

            // create outputstreams
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bos);

            // save data
            CompressedStreamTools.writeTo(tagCompound, data);
            return bos;
        } catch (Exception var5) {
            logger.warning("Failed to write ByteArrayOutputStream for " + entityPlayer.name);
            return null;
        }
    }

    public NBTTagCompound getNBTTagFromStream(DataInputStream dataInputStream, String target) {
        // get data from file
        NBTTagCompound tagCompound = this.loadPlayerDataFromStream(dataInputStream, target);
        return tagCompound;
    }

    public void applyInventory(EntityPlayer entityPlayer, NBTTagCompound tagCompound) {
        // add inventorydata to player
        if (tagCompound != null) {
            NBTTagList tagList = tagCompound.getList("Inventory");

            entityPlayer.inventory.items = new ItemStack[36];
            entityPlayer.inventory.armor = new ItemStack[4];

            for (int i = 0; i < tagList.size(); i++) {
                NBTTagCompound stackCompound = (NBTTagCompound) tagList.get(i);
                int j = stackCompound.getByte("Slot") & 0xFF;
                ItemStack itemstack = ItemStack.createStack(stackCompound);

                if (itemstack != null) {
                    if ((j >= 0) && (j < entityPlayer.inventory.items.length)) {
                        entityPlayer.inventory.items[j] = itemstack;
                    }

                    if ((j >= 100) && (j < entityPlayer.inventory.armor.length + 100)) {
                        entityPlayer.inventory.armor[(j - 100)] = itemstack;
                    }
                }
            }
        }
    }

    /**
     * Gets the player data for the given playername as a NBTTagCompound.
     */
    private NBTTagCompound loadPlayerDataFromStream(DataInputStream dataInputStream, String playerName) {
        try {
            return CompressedStreamTools.read(dataInputStream);
        } catch (IOException e) {
            logger.warning("Failed to load player data from stream for player '" + playerName + "'!");
        }
        return null;
    }
}
