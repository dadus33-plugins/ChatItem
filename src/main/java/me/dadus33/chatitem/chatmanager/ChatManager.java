package me.dadus33.chatitem.chatmanager;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.utils.Storage;

public abstract class ChatManager {

	public final static char SEPARATOR = ((char) 0x0007);
	
	protected Storage s;
	
	public ChatManager() {
		
	}
	
	public abstract String getName();
	
	public abstract String getId();
	
	public Storage getStorage() {
		return s;
	}
	
	public void load(ChatItem pl, Storage s) {
		this.s = s;
	}
	
	public abstract void unload(ChatItem pl);
}
