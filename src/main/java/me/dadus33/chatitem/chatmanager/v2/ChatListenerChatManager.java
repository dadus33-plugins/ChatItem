package me.dadus33.chatitem.chatmanager.v2;

import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;

public class ChatListenerChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	
	public ChatListenerChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();

		Bukkit.getPluginManager().registerEvents(new ChatListener(this), pl);

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
