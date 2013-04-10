package de.minestar.invsync.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.minestar.minestarlibrary.AbstractCore;

public class InvSyncCore extends AbstractCore {

    public static InvSyncCore INSTANCE;
    public static final String NAME = "InvSync";

    private DataHandler dataHandler;
    private ConnectListener listener;

    private InventoryMessageListener inventoryMessageListener;
    private InventoryPacketHandler inventoryPacketHandler;

    public InvSyncCore() {
        super(NAME);
        INSTANCE = this;
    }

    @Override
    protected boolean createManager() {
        this.dataHandler = new DataHandler();
        this.inventoryPacketHandler = new InventoryPacketHandler(this, "MS|" + InvSyncCore.NAME);
        return super.createManager();
    }

    @Override
    protected boolean createListener() {
        this.listener = new ConnectListener(this.inventoryPacketHandler, this.dataHandler);
        return super.createListener();
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.listener, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, this.inventoryPacketHandler.getChannel());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, this.inventoryPacketHandler.getChannel(), this.inventoryMessageListener);
        return super.registerEvents(pm);
    }

    @Override
    protected boolean commonDisable() {
        this.listener.onShutdown();
        return super.commonDisable();
    }

}
