package me.dadus33.chatitem.chatmanager.v1;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.playerversion.IPlayerVersion;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ProtocolSupportHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ViaVersionHook;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Version;

public class PacketEditingChatManager extends ChatManager {

	private static final HashMap<UUID, String> CLIENTS = new HashMap<>();
	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatEventListener chatEventListener;
	private final ChatPacketManager chatPacketManager;
	private final String brandChannelName = Version.getVersion().isNewerOrEquals(Version.V1_13) ? "minecraft:brand" : "MC|Brand";
    private IPlayerVersion playerVersionAdapter;
	
	public PacketEditingChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();
        packetManager = new ChatItemPacketManager(pl);
		chatEventListener = new ChatEventListener(this);
        chatPacketManager = new ChatPacketManager(this);
        
        pl.getServer().getMessenger().registerIncomingPluginChannel(pl, brandChannelName, (chan, p, msg) -> {
			String client = new String(msg);
			ChatItem.debug("Detected client " + client + " for " + p.getName());
			CLIENTS.put(p.getUniqueId(), client);
		});
        
        //Check for existence of BaseComponent class (only on spigot)
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
        } catch (ClassNotFoundException e) {
            baseComponentAvailable = false;
        }
	}
	
	@Override
	public String getName() {
		return "PacketEditing";
	}
	
	@Override
	public String getId() {
		return "packet";
	}
	
	@Override
	public void load(ChatItem pl, Storage s) {
		super.load(pl, s);

		Bukkit.getPluginManager().registerEvents(chatEventListener, pl);
        packetManager.getPacketManager().addHandler(chatPacketManager);

        if(Bukkit.getPluginManager().getPlugin("ViaVersion") != null){
        	playerVersionAdapter = new ViaVersionHook();
        	pl.getLogger().info("Loading ViaVersion support ...");
        } else if(Bukkit.getPluginManager().getPlugin("ProtocolSupport") != null){
        	playerVersionAdapter = new ProtocolSupportHook();
        	pl.getLogger().info("Loading ProtocolSupport support ...");
        } else {
        	playerVersionAdapter = new DefaultVersionHook();
        }
	}
	
	@Override
	public void unload(ChatItem pl) {
		HandlerList.unregisterAll(chatEventListener);
        packetManager.getPacketManager().removeHandler(chatPacketManager);
		packetManager.getPacketManager().stop();
		pl.getServer().getMessenger().unregisterIncomingPluginChannel(pl, brandChannelName);
	}
	
    public JSONManipulatorCurrent getManipulator(){
        return jsonManipulator;
    }
    
    public IPlayerVersion getPlayerVersionAdapter() {
		return playerVersionAdapter;
	}
    
    public String getClient(Player p) {
    	return CLIENTS.getOrDefault(p.getUniqueId(), "unknow");
    }

    public boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }
}
