package me.dadus33.chatitem.hook;

import me.dadus33.chatitem.ChatItem;

public class ChatControlSupport {

	public static void init(ChatItem pl) {
		try {
			Class<?> clz = Class.forName("org.mineacademy.chatcontrol.settings.Settings$Chat$Grammar");
			clz.getField("CAPITALIZE").setBoolean(null, false);
			clz.getField("INSERT_DOT").setBoolean(null, false);
			pl.getLogger().info("Loaded ChatControl support by disabling 2 options in 'Chat.Grammar' config.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
