package me.dadus33.chatitem.chatmanager.v4;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.dadus33.chatitem.chatmanager.ChatManager;

public class OwnListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncChatEvent e) {
		if (ChatManager.isTestingEnabled() && !ChatManager.isTesting("ownformatter"))
			return;
		if (!ChatManager.isSelected("ownformatter"))
			return;
		if (e.isCancelled())
			return;
		e.setCancelled(true);
		/*Player p = e.getPlayer();
		Component msg = e.message();*/
		
		
	}
}
