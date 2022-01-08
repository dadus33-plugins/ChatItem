package me.dadus33.chatitem.playernamer;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

public interface IPlayerNamer {

	public TextComponent getName(Player p);
	
}
