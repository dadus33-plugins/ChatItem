package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent.ContentModifier;
import me.dadus33.chatitem.hook.DiscordSrvSupport;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.PacketUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureComponentManager implements IComponentManager {

	@Override
	public boolean hasConditions() {
		try {
			for (String cl : Arrays.asList("net.kyori.adventure.text.Component", "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer"))
				Class.forName(cl);
		} catch (Throwable e) { // can't support this, adventure comp not found
			ChatItem.debug("Can't load AdventureComponentManager : " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
	}

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		Component comp = packet.getContent().getSpecificModifier(Component.class).readSafely(0);
		if (comp == null)
			return null;
		try {
			String json = GsonComponentSerializer.gson().serialize(comp);
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			ChatItem.debug("AdventureJSON : " + json);
			if (jsonObj.has("with")) {
				JsonObject next = new JsonObject();
				next.add("extra", jsonObj.get("with"));
				return next.toString();
			}
			return json;
		} catch (JsonParseException e) { // ignore this and just let skip this
			return null;
		}
	}

	@Override
	public Object manageItem(Player p, Chat chat, ChatItemPacket packet, ItemStack item, String json, Storage c) throws Exception {
		ChatAction action = chat.getAction();
		if (action.isItem()) {
			String itemName = ChatManager.getNameOfItem(chat.getPlayer(), item, c);
			ChatItem.debug("NBT tag: " + PacketUtils.getNbtTag(item));
			return manage(p, chat, packet, itemName, HoverEvent.showItem(Key.key(item.getType().getKey().getKey()), item.getAmount(), BinaryTagHolder.of(PacketUtils.getNbtTag(item))), null);
		}
		return manage(p, chat, packet, Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", chat.getPlayer().getName()),
				HoverEvent.showText(Component.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName()))),
				ClickEvent.runCommand(action.getCommand()));
	}

	@Override
	public Object manageEmpty(Player p, Chat chat, ChatItemPacket packet, String json, Storage c) {
		Component builder = Component.text("");
		c.tooltipHand.forEach(s -> builder.append(Component.text(s)));
		Player sender = chat.getPlayer();
		ChatAction action = chat.getAction();
		if (action.isItem()) {
			String handName = c.handName.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName());
			return manage(p, chat, packet, handName, HoverEvent.showText(builder), null);
		}
		return manage(p, chat, packet, Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", chat.getPlayer().getName()),
				HoverEvent.showText(Component.text(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName()))),
				ClickEvent.runCommand(action.getCommand()));
	}

	private Object manage(Player p, Chat chat, ChatItemPacket packet, String replacement, HoverEvent<?> hover, ClickEvent click) {
		ContentModifier<Component> modifier = packet.getContent().getSpecificModifier(Component.class);
		Component comp = modifier.readSafely(0);
		if (comp == null) {
			ChatItem.debug("The component is null.");
			return null;
		}
		comp = checkComponent(comp, hover, click, replacement, chat);
		if(ChatItem.discordSrvSupport && DiscordSrvSupport.isSendingMessage())
			DiscordSrvSupport.sendChatMessage(p, comp, null);
		ChatItem.debug("Result: " + GsonComponentSerializer.gson().serialize(comp));
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(GsonComponentSerializer.gson().serialize(comp)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packet.getPacket();
	}

	private Component checkComponent(Component comp, HoverEvent<?> hover, ClickEvent click, String itemName, Chat chat) {
		if (comp instanceof TextComponent) {
			TextComponent tc = (TextComponent) comp;
			if (ChatManager.containsSeparator(tc.content())) {
				ChatItem.debug("Changing text " + tc.content() + " to " + itemName);
				TextColor color = tc.color();
				comp = tc.content(ChatManager.replaceSeparator(chat, tc.content(), itemName)).hoverEvent(hover);
				if (click != null)
					comp.clickEvent(click);
				comp.append(Component.text("").color(color)); // reset color
			} else
				ChatItem.debug("No insert of text without separator: " + tc.content());
		} else if (comp instanceof TranslatableComponent) {
			TranslatableComponent tc = (TranslatableComponent) comp;
			List<Component> next = new ArrayList<>();
			for (Component extra : tc.args()) {
				next.add(checkComponent(extra, hover, click, itemName, chat));
			}
			comp = tc.args(next);
		} else
			ChatItem.debug("Not valid comp class " + comp.getClass().getSimpleName() + " : " + comp);
		List<Component> next = new ArrayList<>();
		for (Component extra : comp.children()) {
			next.add(checkComponent(extra, hover, click, itemName, chat));
		}
		return comp.children(next);
	}
}
