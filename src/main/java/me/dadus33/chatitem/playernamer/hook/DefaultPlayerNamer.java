package me.dadus33.chatitem.playernamer.hook;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playernamer.IPlayerNamer;
import net.md_5.bungee.api.chat.TextComponent;

public class DefaultPlayerNamer implements IPlayerNamer {

	@Override
	public TextComponent getName(Player p) {
		return new TextComponent(p.getName());
	}
}
