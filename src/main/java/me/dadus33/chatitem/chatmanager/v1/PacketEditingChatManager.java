package me.dadus33.chatitem.chatmanager.v1;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.utils.Storage;

public class PacketEditingChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatEventListener chatEventListener;
	private final ChatPacketManager chatPacketManager;
	
	public PacketEditingChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();
        packetManager = new ChatItemPacketManager(pl);
		chatEventListener = new ChatEventListener(this);
        chatPacketManager = new ChatPacketManager(this);
        
        
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
	}
	
	@Override
	public void unload(ChatItem pl) {
		HandlerList.unregisterAll(chatEventListener);
        packetManager.getPacketManager().removeHandler(chatPacketManager);
		packetManager.getPacketManager().stop();
	}
	
    public JSONManipulatorCurrent getManipulator(){
        return jsonManipulator;
    }

    public boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }
}
