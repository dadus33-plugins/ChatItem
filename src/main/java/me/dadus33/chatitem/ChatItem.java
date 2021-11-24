package me.dadus33.chatitem;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import me.dadus33.chatitem.commands.CIReload;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.listeners.ChatEventListener;
import me.dadus33.chatitem.listeners.ChatListener;
import me.dadus33.chatitem.utils.Storage;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 12;
    private static ChatItem instance;
    private ChatEventListener chatEventListener;
    private ChatListener chatListener;
    private Log4jFilter filter;
    private Storage storage;

    public static void reload(CommandSender sender) {
        ChatItem obj = getInstance();
        obj.saveDefaultConfig();
        obj.reloadConfig();
        obj.storage = new Storage(obj.getConfig());
        obj.chatEventListener.setStorage(obj.storage);
        obj.chatListener.setStorage(obj.storage);
        obj.filter.setStorage(obj.storage);
        if (!obj.storage.RELOAD_MESSAGE.isEmpty())
            sender.sendMessage(obj.storage.RELOAD_MESSAGE);
    }

    public static ChatItem getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        //Save the instance (we're basically a singleton)
        instance = this;

        //Load ProtocolManager
        //pm = ProtocolLibrary.getProtocolManager();

        //Load config
        saveDefaultConfig();
        storage = new Storage(getConfig());
        
        //We halt the use of this system for now, until we can solve the infamous getProtocolVersion issue
        //Till then, users of both ViaVersion and ProtocolSupport should do just fine
        /*if(!protocolSupport && !viaVersion) {
            //We only implement our own way of getting protocol versions if we have no other choice
            pm.addPacketListener(new HandshakeListener(this, ListenerPriority.MONITOR, PacketType.Handshake.Client.SET_PROTOCOL));
        }*/

        //Commands
        Bukkit.getPluginCommand("cireload").setExecutor(new CIReload());

        //Bukkit API listeners
        chatEventListener = new ChatEventListener(storage);
        //Bukkit.getPluginManager().registerEvents(chatEventListener, this);
        Bukkit.getPluginManager().registerEvents(chatListener = new ChatListener(storage), this);

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);
    }
    
    public Storage getStorage() {
		return storage;
	}

    public static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
    
    public static void debug(String msg) {
    	if(getInstance().getConfig().getBoolean("debug", false))
    		getInstance().getLogger().info(msg);
    }
}
