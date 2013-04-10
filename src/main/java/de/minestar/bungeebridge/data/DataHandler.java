package de.minestar.bungeebridge.data;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.FoodMetaData;
import net.minecraft.server.v1_5_R2.InventoryEnderChest;
import net.minecraft.server.v1_5_R2.ItemStack;
import net.minecraft.server.v1_5_R2.NBTTagCompound;
import net.minecraft.server.v1_5_R2.NBTTagList;

import org.bukkit.GameMode;

import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;

public class DataHandler {

    public static DataHandler INSTANCE;

    /** Reference to the logger. */
    private static final Logger logger = Logger.getLogger("Minecraft");

    public DataHandler() {
        DataHandler.INSTANCE = this;
    }

    public NBTTagCompound getDataCompound(EntityPlayer entityPlayer) {
        // save inventory
        NBTTagCompound dataCompound = new NBTTagCompound();

        // write inventory
        {
            NBTTagList tagListInventory = new NBTTagList();
            for (int i = 0; i < entityPlayer.inventory.items.length; i++) {
                if (entityPlayer.inventory.items[i] != null) {
                    NBTTagCompound stackCompound = new NBTTagCompound();
                    stackCompound.setByte("Slot", (byte) i);
                    entityPlayer.inventory.items[i].save(stackCompound);
                    tagListInventory.add(stackCompound);
                }
            }

            // write armor
            for (int i = 0; i < entityPlayer.inventory.armor.length; i++) {
                if (entityPlayer.inventory.armor[i] != null) {
                    NBTTagCompound stackCompound = new NBTTagCompound();
                    stackCompound.setByte("Slot", (byte) (i + 100));
                    entityPlayer.inventory.armor[i].save(stackCompound);
                    tagListInventory.add(stackCompound);
                }
            }

            // set compound
            dataCompound.set("Inventory", tagListInventory);
        }

        // write enderchest-inventory
        {
            NBTTagCompound enderchestDataCompound = new NBTTagCompound();

            InventoryEnderChest enderChest = entityPlayer.getEnderChest();
            for (int i = 0; i < enderChest.getSize(); i++) {
                ItemStack itemStack = enderChest.getItem(i);
                if (itemStack != null) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    nbtTagCompound.setByte("Slot", (byte) i);
                    itemStack.save(nbtTagCompound);
                    enderchestDataCompound.set("Slot_" + i, nbtTagCompound);
                }
            }

            // set compound
            dataCompound.set("EnderInventory", enderchestDataCompound);
        }

        // write some extra-data
        {
            NBTTagCompound extraDataCompound = new NBTTagCompound();
            extraDataCompound.setInt("Health", entityPlayer.getBukkitEntity().getHealth());
            extraDataCompound.setInt("SelectedItemSlot", entityPlayer.getBukkitEntity().getInventory().getHeldItemSlot());
            extraDataCompound.setInt("TotalXP", entityPlayer.getBukkitEntity().getTotalExperience());
            extraDataCompound.setInt("GameMode", entityPlayer.getBukkitEntity().getGameMode().ordinal());

            // set compound
            dataCompound.setCompound("ExtraData", extraDataCompound);
        }

        // write fooddata
        {
            FoodMetaData foodData = entityPlayer.getFoodData();
            NBTTagCompound foodCompound = new NBTTagCompound();
            foodCompound.setInt("foodLevel", foodData.foodLevel);
            foodCompound.setInt("foodTickTimer", foodData.foodTickTimer);
            foodCompound.setFloat("foodSaturationLevel", foodData.saturationLevel);
            foodCompound.setFloat("foodExhaustionLevel", foodData.exhaustionLevel);

            // set compound
            dataCompound.setCompound("FoodData", foodCompound);
        }
        return dataCompound;
    }

    public void applyData(EntityPlayer entityPlayer, NBTTagCompound dataCompound) {
        // add inventorydata to player
        if (dataCompound != null) {
            // set inventory
            {
                NBTTagList inventoryTagList = dataCompound.getList("Inventory");

                entityPlayer.inventory.items = new ItemStack[36];
                entityPlayer.inventory.armor = new ItemStack[4];

                for (int i = 0; i < inventoryTagList.size(); i++) {
                    NBTTagCompound stackCompound = (NBTTagCompound) inventoryTagList.get(i);
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

            // set enderinventory
            {
                NBTTagCompound enderDataCompound = dataCompound.getCompound("EnderInventory");

                InventoryEnderChest enderChest = entityPlayer.getEnderChest();
                for (int i = 0; i < enderChest.getSize(); i++) {
                    // null itemslot
                    enderChest.setItem(i, (ItemStack) null);

                    // load item into slot
                    if (enderDataCompound.hasKey("Slot_" + i)) {
                        NBTTagCompound nbttagcompound = enderDataCompound.getCompound("Slot_" + i);
                        int j = nbttagcompound.getByte("Slot") & 0xFF;

                        if ((j >= 0) && (j < enderChest.getSize())) {
                            enderChest.setItem(j, ItemStack.createStack(nbttagcompound));
                        }
                    }
                }
            }

            // set fooddata
            {
                NBTTagCompound foodDataCompound = dataCompound.getCompound("FoodData");
                entityPlayer.getFoodData().foodLevel = foodDataCompound.getInt("foodLevel");
                entityPlayer.getFoodData().foodTickTimer = foodDataCompound.getInt("foodTickTimer");
                entityPlayer.getFoodData().saturationLevel = foodDataCompound.getFloat("foodSaturationLevel");
                entityPlayer.getFoodData().exhaustionLevel = foodDataCompound.getFloat("foodExhaustionLevel");
            }

            // set extradata
            {
                NBTTagCompound extraDataCompound = dataCompound.getCompound("ExtraData");
                entityPlayer.getBukkitEntity().setHealth(extraDataCompound.getInt("Health"));
                entityPlayer.getBukkitEntity().getInventory().setHeldItemSlot(extraDataCompound.getInt("SelectedItemSlot"));
                entityPlayer.getBukkitEntity().setTotalExperience(extraDataCompound.getInt("TotalXP"));
                GameMode gameMode = GameMode.values()[extraDataCompound.getInt("GameMode")];
                if (gameMode != null) {
                    entityPlayer.getBukkitEntity().setGameMode(gameMode);
                }
            }
        }
    }
    /**
     * Writes the player data to a DataOutputStream from the specified PlayerEntityMP.
     */
    public ByteArrayOutputStream getByteStream(EntityPlayer entityPlayer) {
        try {
            // save data
            NBTTagCompound dataCompound = this.getDataCompound(entityPlayer);

            // create outputstreams
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(bos);

            // save data
            CompressedStreamTools.writeTo(dataCompound, data);
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
