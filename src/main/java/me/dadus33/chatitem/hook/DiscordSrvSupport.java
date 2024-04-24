package me.dadus33.chatitem.hook;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import github.scarsz.discordsrv.DiscordSRV;
import me.dadus33.chatitem.ChatItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class DiscordSrvSupport {
	
	public static boolean isSendingMessage() {
		return ChatItem.getInstance().getStorage().discordSrvSendMsg;
	}

	public static void sendChatMessage(Player p, String message, Event e) {
		DiscordSRV pl = DiscordSRV.getPlugin();
		pl.processChatMessage(p, message, pl.getOptionalChannel("global"), false, e);
	}

	public static void sendChatMessage(Player p, Component message, Event e) {
		DiscordSRV pl = DiscordSRV.getPlugin();
		pl.processChatMessage(p, github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(GsonComponentSerializer.gson().serialize(message)), pl.getOptionalChannel("global"), false, e);
	}
}
