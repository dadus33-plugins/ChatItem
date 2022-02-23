package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import net.md_5.bungee.api.ChatColor;

public class ColorManager {

	public static String getColorString(String input) {
		if(input == null || input.isEmpty())
			return "";
		String str = "";
		if(Version.getVersion().isNewerOrEquals(Version.V1_16) && input.startsWith("x")) { // x mean it's an hex
			ChatItem.debug("Removing char x at begin: " + input.substring(1));
			input = input.substring(1);
			if(input.length() >= 6) { // at least hex
				str += ChatColor.of("#" + input.substring(0, 6)); // get first hex color code
				ChatItem.debug("Str with hex: " + str);
				if(input.length() > 6) // if as another color code
					str += getColorString(input.substring(6)); // get color for after
				return str;
			} else
				ChatItem.debug("Low len: " + input.length());
		} else
			ChatItem.debug("1.15 - for : " + input);
		// not hex
		for(char c : input.toCharArray())
			str += ChatColor.getByChar(c);
		return str;
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
		} else
			ChatItem.debug("1.15 - for : " + input);
		// not hex
		return ChatColor.getByChar(input.charAt(0));
	}
}
