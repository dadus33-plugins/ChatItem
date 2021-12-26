package me.dadus33.chatitem.chatmanager.v1;

import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketListenerV2;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketValidatorV2;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.playerversion.IPlayerVersion;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.DefaultVersionHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ProtocolSupportHook;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.ViaVersionHook;

public class PacketEditingChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
    private IPlayerVersion playerVersionAdapter;
	
	public PacketEditingChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();

        packetManager = new ChatItemPacketManager(pl);
        packetManager.getPacketManager().addHandler(new ChatPacketValidatorV2(this));
        packetManager.getPacketManager().addHandler(new ChatPacketListenerV2(this));
        Bukkit.getPluginManager().registerEvents(new ChatEventListener(this), pl);

        //Check for existence of BaseComponent class (only on spigot)
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
        } catch (ClassNotFoundException e) {
            baseComponentAvailable = false;
        }

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
	public String getName() {
		return "PacketEditing";
	}
	
	@Override
	public String getId() {
		return "packet";
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
