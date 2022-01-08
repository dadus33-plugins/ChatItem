package me.dadus33.chatitem.chatmanager.v2;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.utils.Storage;

public class ChatListenerChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private final ChatListener chatListener;
	private boolean baseComponentAvailable = true;
	
	public ChatListenerChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();
		chatListener = new ChatListener(this);

        //Check for existence of BaseComponent class (only on spigot)
        try {
            Class.forName("net.md_5.bungee.api.chat.BaseComponent");
        } catch (ClassNotFoundException e) {
            baseComponentAvailable = false;
        }
	}
	
	@Override
	public String getName() {
		return "ChatListener";
	}
	
	@Override
	public String getId() {
		return "chat";
	}
	
	@Override
	public void load(ChatItem pl, Storage s) {
		super.load(pl, s);

		Bukkit.getPluginManager().registerEvents(chatListener, pl);
	}
	
	@Override
	public void unload(ChatItem pl) {
		HandlerList.unregisterAll(chatListener);
	}
	
    public JSONManipulator getManipulator(){
        /*
            We used to have 2 kinds of JSONManipulators because of my bad understanding of the 1.7 way of parsing JSON chat
            The interface should however stay as there might be great changes in future versions in JSON parsing (most likely 1.13)
         */
        return jsonManipulator;
        //We just return a new one whenever requested for the moment, should implement a cache of some sort some time though
    }

    public boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }
}
