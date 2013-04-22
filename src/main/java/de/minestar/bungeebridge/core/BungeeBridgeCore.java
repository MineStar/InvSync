package de.minestar.bungeebridge.core;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import de.minestar.bungeebridge.commands.cmdGoTo;
import de.minestar.bungeebridge.data.DataHandler;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.bungeebridge.listener.ActionListener;
import de.minestar.bungeebridge.listener.DataMessageListener;
import de.minestar.bungeebridge.listener.StatisticListener;
import de.minestar.bungeebridge.manager.DatabaseManager;
import de.minestar.bungeebridge.manager.StatisticManager;
import de.minestar.minestarlibrary.AbstractCore;
import de.minestar.minestarlibrary.commands.CommandList;

public class BungeeBridgeCore extends AbstractCore {

    public static BungeeBridgeCore INSTANCE;
    public static final String NAME = "InvSync";

    public static boolean SYNC_CHAT = true;
    public static boolean SYNC_DEATH = true;
    public static boolean SYNC_DATA = true;
    public static boolean SYNC_STATS = true;

    private ActionListener listener;

    // PACKETHANDLING
    private DataMessageListener dataMessageListener;
    private DataPacketHandler dataPacketHandler;

    // MANAGER
    private StatisticManager statisticManager;
    private DatabaseManager databaseManager;

    private StatisticListener blockListener;

    public BungeeBridgeCore() {
        super(NAME);
        INSTANCE = this;
    }

    @Override
    protected boolean createCommands() {
        //@formatter:off
        cmdList = new CommandList(NAME,
                // USER PUNISH COMMANDS
                new cmdGoTo             ("/goto",       "<ServerName>",   "invsync.commands.goto", this.dataPacketHandler)
            );
            //@formatter:on
        return true;
    }

    private void loadConfig() {
        try {
            File file = new File(this.getDataFolder(), "sync_settings.yml");
            if (!file.exists()) {
                this.createConfig(file);
                return;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.load(file);
            BungeeBridgeCore.SYNC_CHAT = config.getBoolean("sync.chat", BungeeBridgeCore.SYNC_CHAT);
            BungeeBridgeCore.SYNC_DEATH = config.getBoolean("sync.death", BungeeBridgeCore.SYNC_DEATH);
            BungeeBridgeCore.SYNC_DATA = config.getBoolean("sync.data", BungeeBridgeCore.SYNC_DATA);
            BungeeBridgeCore.SYNC_STATS = config.getBoolean("sync.stats", BungeeBridgeCore.SYNC_STATS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createConfig(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }

            YamlConfiguration config = new YamlConfiguration();
            config.set("sync.chat", BungeeBridgeCore.SYNC_CHAT);
            config.set("sync.death", BungeeBridgeCore.SYNC_DEATH);
            config.set("sync.data", BungeeBridgeCore.SYNC_DATA);
            config.set("sync.stats", BungeeBridgeCore.SYNC_STATS);
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean createManager() {
        this.loadConfig();
        new DataHandler();
        this.dataPacketHandler = new DataPacketHandler(this, "MS_InvSync");

        this.databaseManager = new DatabaseManager(NAME, new File(getDataFolder(), "sqlconfig.yml"));
        this.statisticManager = new StatisticManager(this.databaseManager);
        this.databaseManager.initManager(this.statisticManager);

        return super.createManager();
    }

    @Override
    protected boolean createListener() {
        this.listener = new ActionListener(this.dataPacketHandler);
        this.dataMessageListener = new DataMessageListener(this.dataPacketHandler);
        this.blockListener = new StatisticListener(this.statisticManager);
        return super.createListener();
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.blockListener, this);
        pm.registerEvents(this.listener, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, this.dataPacketHandler.getChannel());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, this.dataPacketHandler.getChannel(), this.dataMessageListener);
        return super.registerEvents(pm);
    }

    @Override
    protected boolean commonDisable() {
        this.statisticManager.saveAllStatistics();
        return super.commonDisable();
    }

    public static StatisticManager getStatisticManager() {
        return INSTANCE.statisticManager;
    }

    public static DatabaseManager getDatabaseManager() {
        return INSTANCE.databaseManager;
    }
}
