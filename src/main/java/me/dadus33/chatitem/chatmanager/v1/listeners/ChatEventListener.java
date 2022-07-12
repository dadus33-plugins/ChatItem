package me.dadus33.chatitem.chatmanager.v1.listeners;

import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR;

import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;

public class ChatEventListener implements Listener {

	private final PacketEditingChatManager manage;

	public ChatEventListener(PacketEditingChatManager manage) {
		this.manage = manage;
	}

	public Storage getStorage() {
		return this.manage.getStorage();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) // We need to have lowest priority in order
																			// to get to the event before DeluxeChat or
																			// other plugins do
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.getMessage().indexOf(SEPARATOR) != -1) { // If the BELL character is found, we have to remove it
			String msg = e.getMessage().replace(Character.toString(SEPARATOR), "");
			ChatItem.debug("Already bell in message: " + e.getMessage());
			e.setMessage(msg);
		}
		boolean found = false;
		final String oldMsg = e.getMessage();
		for (String rep : getStorage().PLACEHOLDERS)
			if (oldMsg.contains(rep)) {
				found = true;
				break;
			}

		if (!found) {
			ChatItem.debug("(v1) not found placeholders in: " + e.getMessage());
			return;
		}

		Player p = e.getPlayer();
		ItemStack item = ChatManager.getUsableItem(p);
		if(!ChatManager.canShowItem(p, item, e))
			return;
		String s = e.getMessage(), firstPlaceholder = getStorage().PLACEHOLDERS.get(0);
		for (String placeholder : getStorage().PLACEHOLDERS) {
			s = s.replace(placeholder, firstPlaceholder);
		}
		if (Utils.countMatches(s, firstPlaceholder) > getStorage().LIMIT) {
			e.setCancelled(true);
			if (getStorage().LIMIT_MESSAGE.isEmpty()) {
				return;
			}
			p.sendMessage(getStorage().LIMIT_MESSAGE);
			return;
		}

		ChatItem.debug("(v1) Set placeholder: " + e.getMessage());
		try {
			StringJoiner msg = new StringJoiner(" ");
			for(String part : s.split(" ")) {
				if(part.equalsIgnoreCase(firstPlaceholder)) {
					msg.add(firstPlaceholder + SEPARATOR + p.getName());
				} else {
					msg.add(part);
				}
			}
			e.setMessage(msg.toString());
			e.setFormat(e.getFormat().replace(oldMsg, e.getMessage())); // set own message for plugin that already put the message into the format
			ChatItem.debug("Message: " + e.getMessage().replace(SEPARATOR, 'S') + ", format: " + e.getFormat());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(final PlayerCommandPreprocessEvent e) {
		if (e.getMessage().indexOf(SEPARATOR) != -1) { // If the BELL character is found, we have to remove it
			String msg = e.getMessage().replace(Character.toString(SEPARATOR), "");
			e.setMessage(msg);
		}
		String commandString = e.getMessage().split(" ")[0].replaceAll("^/+", ""); // First part of the command, without
																					// leading slashes and without
																					// arguments
		Command cmd = Bukkit.getPluginCommand(commandString);
		if (cmd == null) { // not a plugin command
			if (!getStorage().ALLOWED_DEFAULT_COMMANDS.contains(commandString)) {
				return;
			}
		} else {
			if (!getStorage().ALLOWED_PLUGIN_COMMANDS.contains(cmd)) {
				return;
			}
		}

		Player p = e.getPlayer();
		boolean found = false;

		for (String rep : getStorage().PLACEHOLDERS) {
			if (e.getMessage().contains(rep)) {
				found = true;
				break;
			}
		}

		if (!found) {
			return;
		}
		ItemStack item = ChatManager.getUsableItem(p);
		if(!ChatManager.canShowItem(p, item, e))
			return;
		String s = e.getMessage(), firstPlaceholder = getStorage().PLACEHOLDERS.get(0);
		for (String placeholder : getStorage().PLACEHOLDERS) {
			s = s.replace(placeholder, firstPlaceholder);
		}
		if (Utils.countMatches(s, firstPlaceholder) > getStorage().LIMIT) {
			e.setCancelled(true);
			if (getStorage().LIMIT_MESSAGE.isEmpty()) {
				return;
			}
			p.sendMessage(getStorage().LIMIT_MESSAGE);
			return;
		}

		ChatItem.debug("(v1) Set placeholder: " + e.getMessage());
		try {
			StringJoiner msg = new StringJoiner(" ");
			for(String part : s.split(" ")) {
				if(part.equalsIgnoreCase(firstPlaceholder)) {
					msg.add(firstPlaceholder + SEPARATOR + p.getName());
				} else {
					msg.add(part);
				}
			}
			e.setMessage(msg.toString());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
