package me.dadus33.chatitem.chatmanager.v3;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.utils.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PaperListener implements Listener {

	private PaperChatManager manage;

	public PaperListener(PaperChatManager manage) {
		this.manage = manage;
	}

	public Storage getStorage() {
		return manage.getStorage();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncChatEvent e) {
		if(ChatManager.isTestingEnabled() && !ChatManager.isTesting("paper"))
			return;

		Player p = e.getPlayer();
	    Component message = e.message();
		String rawMessage = PlainTextComponentSerializer.plainText().serialize(message);
		if(ChatManager.containsSeparator(rawMessage)) { // fix for v1
			Chat chat = Chat.getFrom(rawMessage);
			if(chat != null) {
		    	message = message.replaceText(TextReplacementConfig.builder().matchLiteral(ChatManager.SEPARATOR + "" + chat.getId() + ChatManager.SEPARATOR_END).replacement(Component.text(chat.getSlot().getPlaceholders().get(0))).build());
				rawMessage = PlainTextComponentSerializer.plainText().serialize(message);
			}
		}
		ItemSlot slot = ItemSlot.getItemSlotFromMessage(rawMessage);
		ChatItem.debug("(v3) Raw message: " + rawMessage + ", slot: " + slot);
		if (slot == null) // if not found
			return;
		ChatAction action = ChatManager.getChatAction(slot, p);
		if(!ChatManager.canUsePlaceholder(p, action.getItem(), slot, e))
			return;
	    ItemStack item = action.getItem();
	    TextComponent like = Component.text(ChatManager.getNameOfItem(p, item, getStorage())).hoverEvent(action.isItem() ? action.getItem().asHoverEvent() : HoverEvent.showText(Component.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", p.getName()))));
	    if(action.hasCommand())
	    	like.clickEvent(ClickEvent.runCommand(action.getCommand()));
	    for(String s : slot.getPlaceholders())
	    	message = message.replaceText(TextReplacementConfig.builder().matchLiteral(s).replacement(like).build());
	    if(ChatItem.getInstance().getConfig().getBoolean("manager-config.paper.send-ourself", false)) {
	    	for(Audience a : e.viewers().isEmpty() ? Bukkit.getOnlinePlayers() : e.viewers())
	    		a.sendMessage(e.renderer().render(p, p.displayName(), message, a));
	    	e.setCancelled(true);
	    } else
	    	e.message(e.renderer().render(p, p.displayName(), message, p));
	    ChatItem.debug("Changed message to " + PlainTextComponentSerializer.plainText().serialize(message));
		if (getStorage().cooldown > 0 && !p.hasPermission("chatitem.ignore-cooldown"))
			ChatManager.applyCooldown(p);
	}
}
