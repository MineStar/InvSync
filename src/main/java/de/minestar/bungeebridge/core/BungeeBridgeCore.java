package de.minestar.bungeebridge.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import de.minestar.bungeebridge.commands.cmdGoTo;
import de.minestar.bungeebridge.data.DataHandler;
import de.minestar.bungeebridge.data.DataPacketHandler;
import de.minestar.bungeebridge.listener.ActionListener;
import de.minestar.bungeebridge.listener.DataMessageListener;
import de.minestar.minestarlibrary.AbstractCore;
import de.minestar.minestarlibrary.commands.CommandList;

public class BungeeBridgeCore extends AbstractCore {

    public static BungeeBridgeCore INSTANCE;
    public static final String NAME = "InvSync";

    private ActionListener listener;

    private DataMessageListener dataMessageListener;
    private DataPacketHandler dataPacketHandler;

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
