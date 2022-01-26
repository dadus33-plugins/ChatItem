package me.dadus33.chatitem.playernamer.hook;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playernamer.IPlayerNamer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class DefaultPlayerNamer implements IPlayerNamer {

	@Override
	public BaseComponent[] getName(Player p) {
		return new ComponentBuilder(p.getName()).create();
	}
}
