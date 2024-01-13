package me.dadus33.chatitem.hook;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import github.scarsz.discordsrv.DiscordSRV;
import me.dadus33.chatitem.ChatItem;
import net.kyori.adventure.text.Component;

public class DiscordSrvSupport {
	
	public static boolean isSendingMessage() {
		return ChatItem.getInstance().getStorage().discordSrvSendMsg;
	}

	public static void sendChatMessage(Player p, String message, Event e) {
		DiscordSRV pl = DiscordSRV.getPlugin();
		if (DiscordSRV.config().getBooleanElse("UseModernPaperChatEvent", false) && pl.isModernChatEventAvailable()) {
			return;
		}
		pl.processChatMessage(p, message, pl.getOptionalChannel("global"), false, e);
	}

	public static void sendChatMessage(Player p, Component message, Event e) {
		DiscordSRV pl = DiscordSRV.getPlugin();
		if (DiscordSRV.config().getBooleanElse("UseModernPaperChatEvent", false) && pl.isModernChatEventAvailable()) {
			return;
		}
		pl.processChatMessage(p, message, pl.getOptionalChannel("global"), false, e);
	}
}
