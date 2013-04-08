package de.minestar.invsync.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.minestar.minestarlibrary.AbstractCore;

public class InvSyncCore extends AbstractCore {

    public static InvSyncCore INSTANCE;
    public static final String NAME = "InvSync";

    private DataHandler dataHandler;
    private ConnectListener listener;

    public InvSyncCore() {
        super(NAME);
        INSTANCE = this;
    }

    @Override
    protected boolean createManager() {
        this.dataHandler = new DataHandler();
        return super.createManager();
    }

    @Override
    protected boolean createListener() {
        this.listener = new ConnectListener(this, this.dataHandler);
        return super.createListener();
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.listener, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "globalchat");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "globalchat", new MessageListener());
        return super.registerEvents(pm);
    }

    @Override
    protected boolean commonDisable() {
        this.listener.onShutdown();
        return super.commonDisable();
    }

}