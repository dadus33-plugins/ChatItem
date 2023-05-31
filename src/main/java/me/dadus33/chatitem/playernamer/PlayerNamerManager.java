package me.dadus33.chatitem.playernamer;

import me.dadus33.chatitem.playernamer.hook.HexNicksV3PlayerNamer;
import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.playernamer.hook.DefaultPlayerNamer;
import me.dadus33.chatitem.playernamer.hook.HexNicksV2PlayerNamer;
import org.bukkit.plugin.Plugin;

public class PlayerNamerManager {

	private static IPlayerNamer playerNamer;
	public static IPlayerNamer getPlayerNamer() {
		return playerNamer;
	}
	public static void setPlayerNamer(IPlayerNamer playerNamer) {
		PlayerNamerManager.playerNamer = playerNamer;
	}
	
	public static void load(ChatItem pl) {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("HexNicks");


		if (plugin != null) {
			char majorVersion = plugin.getDescription().getVersion().charAt(0);

			if (majorVersion == '3')
				setPlayerNamer(new HexNicksV3PlayerNamer());
			else
				setPlayerNamer(new HexNicksV2PlayerNamer());

			pl.getLogger().info("Enable support for HexNicks plugin v" + majorVersion);
			return;
		}



		setPlayerNamer(new DefaultPlayerNamer());

	}
}
