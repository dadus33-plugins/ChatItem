package me.dadus33.chatitem.chatmanager.v3;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.utils.PacketUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
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
	
	@EventHandler
	public void onChat(AsyncChatEvent e) {
		if(ChatManager.isTestingEnabled() && !ChatManager.isTesting("paper"))
			return;

		Player p = e.getPlayer();
		ItemSlot slot = ItemSlot.getItemSlotFromMessage(PlainTextComponentSerializer.plainText().serialize(e.message()));
		if (slot == null) // if not found
			return;
		ChatAction action = ChatManager.getChatAction(slot, p);
		if(action.isItem() && !ChatManager.canShowItem(p, action.getItem(), slot, e))
			return;
		e.setCancelled(true);
	    Component message = e.message();
	    ItemStack item = action.getItem();
	    ComponentLike like = Component.text(ChatManager.getNameOfItem(p, item, getStorage())).hoverEvent(HoverEvent.showItem(Key.key(item.getType().getKey().getKey()), item.getAmount(), BinaryTagHolder.of(PacketUtils.getNbtTag(item))));
	    for(String s : slot.getPlaceholders())
	    	message = message.replaceText(TextReplacementConfig.builder().matchLiteral(s).replacement(like).build());
	    e.message(message);
		if (getStorage().cooldown > 0 && !p.hasPermission("chatitem.ignore-cooldown"))
			ChatManager.applyCooldown(p);
	}
}
