package me.dadus33.chatitem.hook;

import java.util.List;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.h1dd3nxn1nja.chatmanager.Main;

public class ChatManagerSupport {

	public static void init(ChatItem pl) {
		List<String> whitelist = Main.settings.getConfig().getStringList("Anti_Unicode.Whitelist");
		whitelist.add(ChatManager.SEPARATOR_STR);
		whitelist.add(ChatManager.SEPARATOR_END_STR);
	}
}
