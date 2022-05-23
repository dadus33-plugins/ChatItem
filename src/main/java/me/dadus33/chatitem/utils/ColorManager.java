package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import net.md_5.bungee.api.ChatColor;

public class ColorManager {

	public static String getColorString(String input) {
		if(input == null || input.isEmpty())
			return "";
		StringBuilder str = new StringBuilder();
		if(Version.getVersion().isNewerOrEquals(Version.V1_16) && input.startsWith("x")) { // x mean it's an hex
			ChatItem.debug("Removing char x at begin: " + input.substring(1));
			input = input.substring(1);
			if(input.length() >= 6) { // at least hex
				str.append(ChatColor.of("#" + input.substring(0, 6))); // get first hex color code
				ChatItem.debug("Str with hex: " + str);
				if(input.length() > 6) // if as another color code
					str.append(getColorString(input.substring(6))); // get color for after
				return str.toString();
			} else
				ChatItem.debug("Low len: " + input.length());
		}
		// not hex
		for(char c : input.toCharArray())
			str.append(ChatColor.getByChar(c));
		return str.toString();
	}

	public static ChatColor getColor(String input) {
		if(input == null || input.isEmpty())
			return ChatColor.RESET;
		if(Version.getVersion().isNewerOrEquals(Version.V1_16) && input.startsWith("x")) { // x mean it's an hex
			ChatItem.debug("Removing char x at begin: " + input.substring(1));
			if(input.length() >= 7) { // at least hex, and 7 because we count the "x"
				return ChatColor.of("#" + input.substring(1, 7)); // get first hex color code
			} else
				ChatItem.debug("Low len: " + input.length());
		}
		// not hex
		return ChatColor.getByChar(input.charAt(0));
	}
}
