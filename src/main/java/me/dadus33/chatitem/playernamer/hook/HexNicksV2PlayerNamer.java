package me.dadus33.chatitem.playernamer.hook;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playernamer.IPlayerNamer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

public class HexNicksV2PlayerNamer implements IPlayerNamer {

	@Override
	public BaseComponent[] getName(Player p) {
		try {
			Object api = Class.forName("dev.majek.hexnicks.Nicks").getDeclaredMethod("api").invoke(null);
			CompletableFuture<Component> storedNick = (CompletableFuture<Component>) api.getClass().getDeclaredMethod("getStoredNick", Player.class).invoke(api, p);
			return BungeeComponentSerializer.get().serialize(storedNick.join());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BaseComponent[0];
	}

}
