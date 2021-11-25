package me.dadus33.chatitem;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Strings;

import me.dadus33.chatitem.commands.CIReload;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.listeners.ChatListener;
import me.dadus33.chatitem.listeners.JoinListener;
import me.dadus33.chatitem.namer.NamerManager;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 12;
    private static ChatItem instance;
    private ChatListener chatListener;
    private Log4jFilter filter;
    private Storage storage;
    private boolean hasNewVersion = false;

    public static void reload(CommandSender sender) {
        ChatItem obj = getInstance();
        obj.saveDefaultConfig();
        obj.reloadConfig();
        obj.storage = new Storage(obj.getConfig());
        obj.chatListener.setStorage(obj.storage);
        obj.filter.setStorage(obj.storage);
        NamerManager.load(obj);
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
        getCommand("cireload").setExecutor(new CIReload());
        
        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(chatListener = new ChatListener(storage), this);
        pm.registerEvents(new JoinListener(), this);

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);
        
        NamerManager.load(this);
        
        if(storage.CHECK_UPDATE) {
	        getServer().getScheduler().runTaskAsynchronously(this, () -> {
				String urlName = "https://api.spigotmc.org/legacy/update.php?resource=19064";
				String content = Utils.getFromURL(urlName);
				if(!Strings.isNullOrEmpty(content)) {
	    			hasNewVersion = !content.equalsIgnoreCase(getDescription().getVersion());
	    			if(hasNewVersion) {
	    				getLogger().info(storage.JOIN_UPDATE_MESSAGE);
	    			}
				}
	        });
        }
    }
    
    public Storage getStorage() {
		return storage;
	}
    
    public boolean isHasNewVersion() {
		return hasNewVersion;
	}
    
    public static void debug(String msg) {
    	if(getInstance().getConfig().getBoolean("debug", false))
    		getInstance().getLogger().info(msg);
    }
}
