package me.dadus33.chatitem.chatmanager.v2;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatManager;

public class ChatListenerChatManager extends ChatManager {

	private final ChatListener chatListener;
	private boolean baseComponentAvailable = true;
	
	public ChatListenerChatManager(ChatItem pl) {
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

    public boolean supportsChatComponentApi(){
        return baseComponentAvailable;
    }
}
