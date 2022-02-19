package me.dadus33.chatitem.utils;

import me.dadus33.chatitem.ChatItem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ColorManager {

	public static BaseComponent[] getChatWithHex(String msg) {
		ChatItem.debug("Converting " + msg);
		ComponentBuilder builder = new ComponentBuilder("");
		ChatColor color = ChatColor.WHITE;
		String colorCode = "", text = "";
		boolean waiting = false;
		for(char c : msg.toCharArray()) {
			if(c == '§') { // begin of color
				if(colorCode.isEmpty() && !text.isEmpty()) { // text before this char
					ChatItem.debug("Append " + text);
					builder.append(new ComponentBuilder(text).color(color).create());
					text = "";
				}
				
				waiting = true; // waiting for color code
			} else if(waiting) { // if waiting for code and valid str
				if(String.valueOf(c).matches("-?[0-9a-fA-F]+") && colorCode.length() <= 5) { // if it's hexademical value and with enough space for full color
					colorCode += c; // add char to it
					waiting = false;
				} else {
					color = ChatColor.getByChar(c); // a color by itself
					colorCode = ""; // clean actual code, it's only to prevent some kind of issue
					waiting = false;
				}
			} else {
				if(!colorCode.isEmpty()) {
					color = getColor(colorCode);
				}
				// basic text, not waiting for code after '§'
				text += c;
				colorCode = ""; // clean actual code
				waiting = false;
			}
		}
		if(!text.isEmpty())
			builder.append(new ComponentBuilder(text).color(color).create()); // add last char
		return builder.create();
	}
	
	public static ChatColor getColor(String input) {
		if(input == null || input.isEmpty())
			return ChatColor.RESET;
		if(input.length() == 6)
			return ChatColor.of("#" + input);
		return ChatColor.getByChar(input.charAt(0));
	}
}
