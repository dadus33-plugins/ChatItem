package me.dadus33.chatitem.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;

public class Messages {

	public static void sendMessage(CommandSender p, String key, Object... placeholders) {
		getMessageList(key, placeholders).forEach(p::sendMessage);
	}
	
	public static String getMessage(String key, Object... placeholders) {
		return colorAndPlaceholders(ChatItem.getInstance().getConfig().getString("messages." + key, key), placeholders);
	}
	
	public static List<String> getMessageList(String key, Object... placeholders) {
		Object obj = ChatItem.getInstance().getConfig().get("messages." + key);
		if(obj == null)
			return Arrays.asList(key);
		if(obj instanceof List) {
			return ((List<String>) obj).stream().map((s) -> colorAndPlaceholders(s, placeholders)).collect(Collectors.toList());
		} else if(obj instanceof String)
			return Arrays.asList(colorAndPlaceholders((String) obj, placeholders));
		else
			return Arrays.asList(colorAndPlaceholders(obj.toString(), placeholders));
	}
	
	private static String colorAndPlaceholders(String msg, Object... placeholders) {
		if(msg == null)
			return "";
		for (int index = 0; index <= placeholders.length - 1; index += 2) {
			msg = msg.replace(String.valueOf(placeholders[index]), String.valueOf(placeholders[index + 1]));
		}
		return Storage.color(msg);
	}
}
