package me.dadus33.chatitem.hook.placeholders;

import org.bukkit.entity.Player;

public class OwnPlaceholder implements IPlaceholders {

	@Override
	public String replace(Player p, String text) {
		return text.replace("{name}", p.getName()).replace("%name%", p.getName());
	}

}
