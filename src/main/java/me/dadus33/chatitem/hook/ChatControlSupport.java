package me.dadus33.chatitem.hook;

import org.mineacademy.chatcontrol.settings.Settings;

import me.dadus33.chatitem.ChatItem;

public class ChatControlSupport {

	public static void init(ChatItem pl) {
		Settings.Chat.Grammar.CAPITALIZE = false;
		Settings.Chat.Grammar.INSERT_DOT = false;
		
		pl.getLogger().info("Loaded ChatControl support by disabling 2 options in 'Chat.Grammar' config.");
	}
}
