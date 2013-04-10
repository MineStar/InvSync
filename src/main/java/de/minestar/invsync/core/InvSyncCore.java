package de.minestar.invsync.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.minestar.invsync.data.DataHandler;
import de.minestar.invsync.data.DataPacketHandler;
import de.minestar.invsync.listener.ActionListener;
import de.minestar.invsync.listener.DataMessageListener;
import de.minestar.minestarlibrary.AbstractCore;

public class InvSyncCore extends AbstractCore {

    public static InvSyncCore INSTANCE;
    public static final String NAME = "InvSync";

    private ActionListener listener;

    private DataMessageListener dataMessageListener;
    private DataPacketHandler dataPacketHandler;

    public InvSyncCore() {
        super(NAME);
        INSTANCE = this;
    }

    @Override
    protected boolean createManager() {
        new DataHandler();
        this.dataPacketHandler = new DataPacketHandler(this, "MS_InvSync");
        return super.createManager();
    }

    @Override
    protected boolean createListener() {
        this.listener = new ActionListener(this.dataPacketHandler);
        this.dataMessageListener = new DataMessageListener(this.dataPacketHandler);
        return super.createListener();
    }

    @Override
    protected boolean registerEvents(PluginManager pm) {
        pm.registerEvents(this.listener, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, this.dataPacketHandler.getChannel());
        Bukkit.getMessenger().registerIncomingPluginChannel(this, this.dataPacketHandler.getChannel(), this.dataMessageListener);
        return super.registerEvents(pm);
    }
}
