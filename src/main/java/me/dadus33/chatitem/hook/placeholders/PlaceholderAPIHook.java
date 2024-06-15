package me.dadus33.chatitem.hook.placeholders;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIHook implements IPlaceholders {

	@Override
	public String replace(Player p, String text) {
		return PlaceholderAPI.setPlaceholders(p, text);
	}

}
