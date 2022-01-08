package me.dadus33.chatitem.playernamer.hook;

import org.bukkit.entity.Player;

import dev.majek.hexnicks.Nicks;
import me.dadus33.chatitem.playernamer.IPlayerNamer;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;

public class HexNicksPlayerNamer implements IPlayerNamer {

	@Override
	public TextComponent getName(Player p) {
		return new TextComponent(BungeeComponentSerializer.get().serialize(Nicks.api().getStoredNick(p).join()));
	}

}
