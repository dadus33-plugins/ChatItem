package me.dadus33.chatitem.chatmanager.v2;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.hook.DiscordSrvSupport;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.Utils;
import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("deprecation")
public class ChatListener implements Listener {

	private ChatListenerChatManager manage;

	public ChatListener(ChatListenerChatManager manage) {
		this.manage = manage;
	}

	public Storage getStorage() {
		return manage.getStorage();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		if(ChatManager.isTestingEnabled() && !ChatManager.isTesting("chat"))
			return;
		if(!ChatManager.isSelected("chat"))
			return;
		Storage c = getStorage();
		if (e.isCancelled()) {
			if (ChatItem.getInstance().getChatManager().size() == 1) { // only chat
				String msg = e.getMessage().toLowerCase();
				for(ItemSlot slot : ItemSlot.values()) {
					for (String rep : slot.getPlaceholders()) {
						if (msg.contains(rep)) {
							ChatItem.debug("You choose 'chat' manager but it seems to don't be the good choice. More informations here: https://github.com/dadus33-plugins/ChatItem/wiki");
							return;
						}
					}
				}
			}
			ChatItem.debug("Chat cancelled for " + e.getPlayer().getName());
			return;
		}
		Player p = e.getPlayer();
		String targetReplace = ChatManager.SEPARATOR + "";
		if(ChatManager.containsSeparator(e.getMessage())) { // fix for v1
			Chat chat = Chat.getFrom(e.getMessage());
			if(chat != null) {
				targetReplace = ChatManager.SEPARATOR + "" + chat.getId() + ChatManager.SEPARATOR_END;
				e.setMessage(ChatManager.replaceSeparator(chat, e.getMessage(), chat.getSlot().getPlaceholders().get(0)));
			}
		}
		ItemSlot slot = ItemSlot.getItemSlotFromMessage(e.getMessage());
		if (slot == null) { // if not found
			ChatItem.debug("(v2) Can't found placeholder in: " + e.getMessage() + " > " + PlayerNamerManager.getPlayerNamer().getName(p));
			return;
		}
		ChatAction action = ChatManager.getChatAction(slot, p);
		if(!ChatManager.canUsePlaceholder(p, action, slot, e))
			return;
		e.setCancelled(true);
		String format = e.getFormat().replace(targetReplace, slot.getPlaceholders().get(0));
		ChatItem.debug("(v2) Using format: " + format + " and message: " + e.getMessage());
		String defMsg = slot.replacePlaceholdersToSeparator(e.getMessage());
		if (Utils.countMatches(defMsg, Character.toString(ChatManager.SEPARATOR)) > c.limit) {
			if (!c.messageLimit.isEmpty())
				p.sendMessage(c.messageLimit);
			return;
		}
		String msg;
		if (format.contains("%1$s") || format.contains("%2$s")) {
			msg = (format.contains("%2$s") ? String.format(format, p.getDisplayName(), defMsg) : String.format(format, p.getDisplayName()));
		} else {
			msg = format.replace(e.getMessage(), defMsg);
		}
		String itemName = ChatManager.getNameForChatAction(p, action, c);
		String loggedMessage = msg.replace(ChatManager.SEPARATOR + "", itemName);
		Bukkit.getConsoleSender().sendMessage(c.replaceInConsole ? loggedMessage : msg); // show in log
		if (ChatItem.discordSrvSupport)
			DiscordSrvSupport.sendChatMessage(p, defMsg.replace(ChatManager.SEPARATOR + "", itemName), e);
		Set<Player> recipients = e.getRecipients().isEmpty() ? new HashSet<>(Bukkit.getOnlinePlayers()) : e.getRecipients();
		ChatItem.debug("(v2) Msg: " + ChatItem.replace(p, msg).replace(ChatColor.COLOR_CHAR, '&') + ", format: " + format + " to " + recipients.size() + " players");
		recipients.forEach((pl) -> ChatItem.getPlatform().sendMessage(pl, p, action, ChatItem.replace(p, msg)));
		if (c.cooldown > 0 && !p.hasPermission("chatitem.ignore-cooldown"))
			ChatManager.applyCooldown(p);
	}
}
