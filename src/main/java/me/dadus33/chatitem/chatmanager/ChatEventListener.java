package me.dadus33.chatitem.chatmanager;

import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR;
import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR_END;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.utils.Utils;

public class ChatEventListener implements Listener {

	private Storage getStorage() {
		return ChatItem.getInstance().getStorage();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (ChatManager.isTestingEnabled() && !ChatManager.isTesting("packet"))
			return;
		if (ChatManager.containsSeparator(e.getMessage())) { // If the BELL character is found, we have to remove it
			e.setMessage(ChatManager.removeSeparator(e.getMessage()));
		}
		Player p = e.getPlayer();
		String oldMsg = e.getMessage();
		ItemSlot slot = ItemSlot.getItemSlotFromMessage(e.getMessage());
		if (slot == null) {
			ChatItem.debug("(v1) not found placeholders in: " + e.getMessage());
			return;
		}
		ItemStack item = ChatManager.getUsableItem(p, slot);
		if (!ChatManager.canShowItem(p, item, slot, e))
			return;
		String s = slot.replacePlaceholdersToSeparator(e.getMessage());
		if (Utils.countMatches(s, Character.toString(ChatManager.SEPARATOR)) > getStorage().limit) {
			e.setCancelled(true);
			if (!getStorage().messageLimit.isEmpty())
				p.sendMessage(getStorage().messageLimit);
			return;
		}

		Chat c = Chat.create(p, oldMsg, slot);
		ChatItem.debug("(v1) Set placeholder to message " + c);
		e.setMessage(s.replace(Character.toString(ChatManager.SEPARATOR), SEPARATOR + Integer.toString(c.getId()) + SEPARATOR_END));
		e.setFormat(e.getFormat().replace(oldMsg, e.getMessage())); // set own message for plugin that already put the message into the format
	}
}
