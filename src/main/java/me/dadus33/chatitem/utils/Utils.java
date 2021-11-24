package me.dadus33.chatitem.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {

	public static String coloredMessage(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers() {
		Object players = Bukkit.getOnlinePlayers();
		if(players instanceof Player[])
			return Arrays.asList((Player[]) players);
		return new ArrayList<>((Collection<Player>) players);
	}
}
