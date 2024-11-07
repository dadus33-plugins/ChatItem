package me.dadus33.chatitem.chatmanager.v4;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;

public class OwnManager extends ChatManager {

	@Override
	public String getName() {
		return "Own Formatter";
	}

	@Override
	public String getId() {
		return "ownformatter";
	}

	@Override
	public void unload(ChatItem pl) {
		
	}
}
