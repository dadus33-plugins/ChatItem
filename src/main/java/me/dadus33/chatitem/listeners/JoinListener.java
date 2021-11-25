package me.dadus33.chatitem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import me.dadus33.chatitem.ChatItem;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class JoinListener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!p.isOp())
			return;
		ChatItem pl = ChatItem.getInstance();
		if(pl.isHasNewVersion()) {
			TextComponent text = new TextComponent(pl.getStorage().JOIN_UPDATE_MESSAGE);
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(pl.getStorage().JOIN_UPDATE_HOVER).create()));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/19064/"));
			p.spigot().sendMessage(text);
		}
	}
}
