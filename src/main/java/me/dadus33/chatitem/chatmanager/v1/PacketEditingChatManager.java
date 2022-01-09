package me.dadus33.chatitem.chatmanager.v1;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.chatmanager.v1.listeners.v15lower.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.v15lower.ChatPacket15Manager;
import me.dadus33.chatitem.chatmanager.v1.listeners.v16upper.ChatPacket16Manager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketHandler;
import me.dadus33.chatitem.chatmanager.v1.playerversion.IPlayerVersion;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ProtocolSupportHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ViaVersionHook;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Version;

public class PacketEditingChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatEventListener chatEventListener;
	private final PacketHandler chatPacketManager;
    private IPlayerVersion playerVersionAdapter;
	
	public PacketEditingChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();
        packetManager = new ChatItemPacketManager(pl);
        if(Version.getVersion().isNewerOrEquals(Version.V1_16)) {
            chatPacketManager = new ChatPacket16Manager(this);
    		chatEventListener = null;
        } else {
    		chatEventListener = new ChatEventListener(this);
            chatPacketManager = new ChatPacket15Manager(this);
        }
        
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

		if(chatEventListener != null)
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
		if(chatEventListener != null)
			HandlerList.unregisterAll(chatEventListener);
        packetManager.getPacketManager().removeHandler(chatPacketManager);
		packetManager.getPacketManager().stop();
	}
	
    public JSONManipulator getManipulator(){
        /*
            We used to have 2 kinds of JSONManipulators because of my bad understanding of the 1.7 way of parsing JSON chat
            The interface should however stay as there might be great changes in future versions in JSON parsing (most likely 1.13)
         */
        return jsonManipulator;
        //We just return a new one whenever requested for the moment, should implement a cache of some sort some time though
    }
    
    public IPlayerVersion getPlayerVersionAdapter() {
		return playerVersionAdapter;
	}

    public boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }
}
