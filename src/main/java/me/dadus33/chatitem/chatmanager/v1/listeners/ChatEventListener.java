package me.dadus33.chatitem.chatmanager.v1.listeners;

import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR;
import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR_END;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.utils.Utils;

public class ChatEventListener implements Listener {

	private final PacketEditingChatManager manage;

	public ChatEventListener(PacketEditingChatManager manage) {
		this.manage = manage;
	}

	public Storage getStorage() {
		return this.manage.getStorage();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if(ChatManager.isTestingEnabled() && !ChatManager.isTesting("packet"))
			return;
		if (ChatManager.containsSeparator(e.getMessage())) { // If the BELL character is found, we have to remove it
			e.setMessage(ChatManager.removeSeparator(e.getMessage()));
		}
		boolean found = false;
		final String oldMsg = e.getMessage();
		for (String rep : getStorage().placeholders) {
			if (oldMsg.contains(rep)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ChatItem.debug("(v1) not found placeholders in: " + e.getMessage());
			return;
		}

		Player p = e.getPlayer();
		ItemStack item = ChatManager.getUsableItem(p);
		if (!ChatManager.canShowItem(p, item, e))
			return;
		String s = e.getMessage(), firstPlaceholder = getStorage().placeholders.get(0);
		for (String placeholder : getStorage().placeholders) {
			s = s.replace(placeholder, firstPlaceholder);
		}
		if (Utils.countMatches(s, firstPlaceholder) > getStorage().limit) {
			e.setCancelled(true);
			if (!getStorage().messageLimit.isEmpty())
				p.sendMessage(getStorage().messageLimit);
			return;
		}

		Chat c = Chat.create(p, oldMsg);
		ChatItem.debug("(v1) Set placeholder to message " + c);
		e.setMessage(s.replace(firstPlaceholder, SEPARATOR + Integer.toString(c.getId()) + SEPARATOR_END));
		e.setFormat(e.getFormat().replace(oldMsg, e.getMessage())); // set own message for plugin that already put the message into the format
	}
}
