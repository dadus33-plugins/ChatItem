package me.dadus33.chatitem.hook.placeholders;

import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;

public class MVdWPlaceholderAPIHook implements IPlaceholders {

	@Override
	public String replace(Player p, String text) {
		return PlaceholderAPI.replacePlaceholders(p, text);
	}
}
