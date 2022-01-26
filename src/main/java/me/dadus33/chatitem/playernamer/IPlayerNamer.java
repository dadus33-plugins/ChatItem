package me.dadus33.chatitem.playernamer;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

public interface IPlayerNamer {

	public BaseComponent[] getName(Player p);
	
}
