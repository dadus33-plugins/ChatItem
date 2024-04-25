package me.dadus33.chatitem.chatmanager.v3;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatManager;

public class PaperChatManager extends ChatManager {

	private PaperListener paperListener;
	
	public PaperChatManager(ChatItem pl) {
		paperListener = new PaperListener(this);
	}
	
	@Override
	public String getName() {
		return "Paper";
	}

	@Override
	public String getId() {
		return "paper";
	}
	
	@Override
	public void load(ChatItem pl, Storage s) {
		super.load(pl, s);

		Bukkit.getPluginManager().registerEvents(paperListener, pl);
	}

	@Override
	public void unload(ChatItem pl) {
		HandlerList.unregisterAll(paperListener);
	}

}
