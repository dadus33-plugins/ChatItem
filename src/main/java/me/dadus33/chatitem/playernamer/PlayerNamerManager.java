package me.dadus33.chatitem.playernamer;

import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.playernamer.hook.DefaultPlayerNamer;
import me.dadus33.chatitem.playernamer.hook.HexNicksPlayerNamer;

public class PlayerNamerManager {

	private static IPlayerNamer playerNamer;
	public static IPlayerNamer getPlayerNamer() {
		return playerNamer;
	}
	public static void setPlayerNamer(IPlayerNamer playerNamer) {
		PlayerNamerManager.playerNamer = playerNamer;
	}
	
	public static void load(ChatItem pl) {
		if(Bukkit.getPluginManager().getPlugin("HexNicks") != null) {
			setPlayerNamer(new HexNicksPlayerNamer());
		} else
			setPlayerNamer(new DefaultPlayerNamer());
	}
}
