package me.dadus33.chatitem;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import me.dadus33.chatitem.api.APIImplementation;
import me.dadus33.chatitem.api.ChatItemAPI;
import me.dadus33.chatitem.commands.CIReload;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.listeners.ChatEventListener;
import me.dadus33.chatitem.listeners.ChatListener;
import me.dadus33.chatitem.packets.ChatItemPacketManager;
import me.dadus33.chatitem.playerversion.IPlayerVersion;
import me.dadus33.chatitem.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.playerversion.hooks.ProtocolSupportHook;
import me.dadus33.chatitem.playerversion.hooks.ViaVersionHook;
import me.dadus33.chatitem.utils.Storage;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 12;
    private static ChatItem instance;
    private ChatEventListener chatEventListener;
    private ChatListener chatListener;
    private Log4jFilter filter;
    private Storage storage;
    private static boolean baseComponentAvailable = true;
    private IPlayerVersion playerVersionAdapter;
    private ChatItemPacketManager packetManager;

    public static void reload(CommandSender sender) {
        ChatItem obj = getInstance();
        //obj.pm = ProtocolLibrary.getProtocolManager();
        obj.saveDefaultConfig();
        obj.reloadConfig();
        obj.storage = new Storage(obj.getConfig());
        //obj.packetListener.setStorage(obj.storage);
        //obj.packetValidator.setStorage(obj.storage);
        obj.chatEventListener.setStorage(obj.storage);
        obj.chatListener.setStorage(obj.storage);
        obj.filter.setStorage(obj.storage);
        APIImplementation api = (APIImplementation) Bukkit.getServicesManager().getRegistration(ChatItemAPI.class).getProvider();
        api.setStorage(obj.storage);
        api.updateLogger();
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

        //Load API
        APIImplementation api = new APIImplementation(storage);
        Bukkit.getServicesManager().register(ChatItemAPI.class, api, this, ServicePriority.Highest);

        //Packet listeners
        packetManager = new ChatItemPacketManager(this);
        //packetManager.getPacketManager().addHandler(new ChatPacketValidatorV2(storage));
        //packetManager.getPacketManager().addHandler(new ChatPacketListenerV2(storage));
        /*packetListener = new ChatPacketListener(this, ListenerPriority.LOW, storage, PacketType.Play.Server.CHAT);
        packetValidator = new ChatPacketValidator(this, ListenerPriority.LOWEST, storage, PacketType.Play.Server.CHAT);
        pm.addPacketListener(packetValidator);
        pm.addPacketListener(packetListener);*/

        if(Bukkit.getPluginManager().getPlugin("ViaVersion") != null){
        	playerVersionAdapter = new ViaVersionHook();
        } else if(Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null){
        	playerVersionAdapter = new ProtocolSupportHook();
        } else {
        	playerVersionAdapter = new DefaultVersionHook();
        }

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

        //Check for existence of BaseComponent class (only on spigot)
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
        } catch (ClassNotFoundException e) {
            baseComponentAvailable = false;
        }

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);
    }
    
    public Storage getStorage() {
		return storage;
	}
    
    public IPlayerVersion getPlayerVersionAdapter() {
		return playerVersionAdapter;
	}
    
    public ChatItemPacketManager getPacketManager() {
		return packetManager;
	}

    public static String getVersion(Server server) {
        final String packageName = server.getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }

    public static JSONManipulatorCurrent getManipulator(){
        /*
            We used to have 2 kinds of JSONManipulators because of my bad understanding of the 1.7 way of parsing JSON chat
            The interface should however stay as there might be great changes in future versions in JSON parsing (most likely 1.13)
         */
        return new JSONManipulatorCurrent();
        //We just return a new one whenever requested for the moment, should implement a cache of some sort some time though
    }
    
    public static void debug(String msg) {
    	if(getInstance().getConfig().getBoolean("debug", false))
    		getInstance().getLogger().info(msg);
    }
}
