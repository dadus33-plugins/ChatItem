package me.dadus33.chatitem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Strings;

import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v2.ChatListenerChatManager;
import me.dadus33.chatitem.commands.CIReloadCommand;
import me.dadus33.chatitem.commands.ChatItemCommand;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.listeners.InventoryListener;
import me.dadus33.chatitem.listeners.JoinListener;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.SemVer;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 13;
    private static ChatItem instance;
    private Log4jFilter filter;
    private Storage storage;
    private boolean hasNewVersion = false;
    private List<ChatManager> chatManager = new ArrayList<>();

    private void chooseManagers() {
    	this.chatManager.forEach((cm) -> cm.unload(this));
    	this.chatManager.clear();
    	PluginManager pm = getServer().getPluginManager();
        String managerName = getStorage().MANAGER;
        if(managerName.equalsIgnoreCase("both")) {
        	this.chatManager.add(new PacketEditingChatManager(this));
	        this.chatManager.add(new ChatListenerChatManager(this));
            getLogger().info("Manager automatically choosed: " + getVisualChatManagers());
        } else if(managerName.equalsIgnoreCase("auto")) {
            if(getPluginThatRequirePacket().stream().map(pm::getPlugin).filter(Objects::nonNull).count() > 0 && Version.getVersion().isNewerThan(Version.V1_7))
            	this.chatManager.add(new PacketEditingChatManager(this));
            else
            	this.chatManager.add(new ChatListenerChatManager(this));
            getLogger().info("Manager automatically choosed: " + getVisualChatManagers());
        } else {
            if(managerName.equalsIgnoreCase("packet")) {
            	this.chatManager.add(new PacketEditingChatManager(this));
                getLogger().info("Manager choosed: " + getVisualChatManagers());
            } else if(managerName.equalsIgnoreCase("chat")) {
            	this.chatManager.add(new ChatListenerChatManager(this));
                getLogger().info("Manager choosed: " + getVisualChatManagers());
            } else {
            	getLogger().severe("----- WARN -----");
            	getLogger().severe("Failed to find manager: " + managerName + ".");
            	getLogger().severe("Please reset your config and/or check wiki for more informations");
            	getLogger().severe("Using default manager: chat.");
            	getLogger().severe("----- WARN -----");
            	this.chatManager.add(new ChatListenerChatManager(this));
            }
        }
        this.chatManager.forEach((cm) -> cm.load(this, getStorage()));

        NamerManager.load(this);
        PlayerNamerManager.load(this);
    }
    
    private List<String> getPluginThatRequirePacket(){
    	return Arrays.asList("DeluxeChat", "HexNicks", "VentureChat");
    }
    
    public static void reload(CommandSender sender) {
        ChatItem pl = getInstance();
        if(pl.getConfig() == null || pl.getConfig().getKeys(false).isEmpty())
        	pl.saveDefaultConfig();
        pl.reloadConfig();
        String oldChatManager = pl.storage.MANAGER;
        pl.storage = new Storage(pl.getConfig());
        pl.chooseManagers();
        pl.filter.setStorage(pl.storage);
        if (!pl.storage.RELOAD_MESSAGE.isEmpty())
            sender.sendMessage(pl.storage.RELOAD_MESSAGE);
        if(!oldChatManager.equalsIgnoreCase(pl.storage.MANAGER))
        	sender.sendMessage(ChatColor.GOLD + "Changing the manager with command reloading CAN produce issue. It's mostly suggested to restart after finding the better manager for you.");
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
        loadCommand(getCommand("cireload"), new CIReloadCommand());
        loadCommand(getCommand("chatitem"), new ChatItemCommand());
        
        // events
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        
        chooseManagers();

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);
        
        if(storage.CHECK_UPDATE) {
	        getServer().getScheduler().runTaskAsynchronously(this, () -> {
				String urlName = "https://api.spigotmc.org/legacy/update.php?resource=19064";
				String content = Utils.getFromURL(urlName);
				if(Strings.isNullOrEmpty(content))
					return;
				SemVer currentVersion = SemVer.parse(getDescription().getVersion());
				if (currentVersion == null)
					return;
				SemVer latestVersion = SemVer.parse(content);
				if (latestVersion != null  && latestVersion.isNewerThan(currentVersion)) {
	    			hasNewVersion = !content.equalsIgnoreCase(getDescription().getVersion());
	    			if(hasNewVersion) {
	    				getLogger().info(storage.JOIN_UPDATE_MESSAGE);
	    			}
				}
	        });
        }
    }
	
	public void loadCommand(PluginCommand cmd, Object obj) {
		if(obj instanceof CommandExecutor)
			cmd.setExecutor((CommandExecutor) obj);
		if(obj instanceof TabCompleter)
			cmd.setTabCompleter((TabCompleter) obj);
	}
    
    public Storage getStorage() {
		return storage;
	}
    
    public List<ChatManager> getChatManager() {
		return chatManager;
	}
    
    public String getVisualChatManagers() {
    	StringJoiner sj = new StringJoiner(", ");
    	for(ChatManager cm : chatManager) {
    		sj.add(cm.getName() + " (" + cm.getId() + ")");
    	}
    	return sj.toString();
    }
    
    public boolean isHasNewVersion() {
		return hasNewVersion;
	}
    
    public static void debug(String msg) {
    	if(getInstance().getConfig().getBoolean("debug", false))
    		getInstance().getLogger().info("[Debug] " + msg);
    }
}
