package me.dadus33.chatitem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class JoinListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!p.isOp())
			return;
		ChatItem pl = ChatItem.getInstance();
		if(pl.isHasNewVersion()) {
			TextComponent text = new TextComponent(pl.getStorage().JOIN_UPDATE_MESSAGE);
			text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(pl.getStorage().JOIN_UPDATE_HOVER).create())));
			text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/19064/"));
			p.spigot().sendMessage(text);
		}
	}
	
	@EventHandler
	public void onLeft(PlayerQuitEvent e) {
		ChatManager.clear(e.getPlayer());
	}
}
