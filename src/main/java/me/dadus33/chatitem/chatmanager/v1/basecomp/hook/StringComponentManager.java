package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatAction;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class StringComponentManager implements IComponentManager {

	@Override
	public String getBaseComponentAsJSON(ChatItemPacket packet) {
		String json = packet.getContent().getStrings().readSafely(0);
		if (json != null && json.startsWith("[") && json.endsWith("]")) { // if used as array instead of json obj
			JsonArray extra = new JsonArray();
			for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
				if (element.isJsonObject()) { // ignore this
					extra.add(element);
				} else {
					ChatItem.debug("Ignoring element " + element);
				}
			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("text", "");
			jsonObject.add("extra", extra);
			json = jsonObject.toString();
		} else if (json == null) {
			BaseComponent[] comp = packet.getContent().getSpecificModifier(BaseComponent[].class).readSafely(0);
			if (comp != null)
				return ComponentSerializer.toString(comp);
		}
		return json;
	}

	@Override
	public void writeJson(ChatItemPacket packet, String json) {
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(json));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object manageItem(Player p, Chat chat, ChatItemPacket packet, ItemStack item, String json, Storage c) throws Exception {
		ChatAction action = chat.getAction();
		if (action.isItem())
			return manage(p, chat, packet, ChatManager.getNameOfItem(chat.getPlayer(), item, c), Utils.createItemHover(item, p), null);
		else
			return manage(p, chat, packet, ChatManager.getNameOfItem(chat.getPlayer(), item, c),
					Utils.createTextHover(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName())),
					Utils.createRunCommand(action.getCommand()));
	}

	@Override
	public Object manageEmpty(Player p, Chat chat, ChatItemPacket packet, String json, Storage c) {
		ComponentBuilder builder = new ComponentBuilder("");
		c.tooltipHand.forEach(s -> builder.append(s));
		Player sender = chat.getPlayer();
		HoverEvent hover;
		ClickEvent click;
		String rep;
		ChatAction action = chat.getAction();
		if (action.isItem()) {
			hover = Utils.createTextHover(builder.create());
			rep = c.handName.replace("{name}", sender.getName()).replace("{display-name}", sender.getDisplayName());
			click = null;
		} else {
			hover = Utils.createTextHover(Messages.getMessage(action.getSlot().name().toLowerCase() + ".hover", "%cible%", chat.getPlayer().getName()));
			rep = Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", chat.getPlayer().getName());
			click = Utils.createRunCommand(action.getCommand());
		}
		return manage(p, chat, packet, rep, hover, click);
	}

	private Object manage(Player p, Chat chat, ChatItemPacket packet, String replacement, HoverEvent hover, ClickEvent click) {
		BaseComponent[] components = packet.getContent().getSpecificModifier(BaseComponent[].class).readSafely(0);
		if (components == null) {
			String json = packet.getContent().getStrings().readSafely(0);
			if (json != null && json.startsWith("[") && json.endsWith("]")) { // if used as array instead of json obj
				JsonArray extra = new JsonArray();
				for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
					if (element.isJsonObject()) { // ignore this
						extra.add(element);
					} else {
						ChatItem.debug("Ignoring element " + element);
					}
				}
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("text", "");
				jsonObject.add("extra", extra);
				json = jsonObject.toString();
			}
			ChatItem.debug("[StringComponentManager] Using JSON " + json);
			try {
				components = ComponentSerializer.parse(json);
			} catch (Exception e) {
				ChatItem.getInstance().getLogger().severe("Failed to parse JSON: " + json + ". Error:");
				e.printStackTrace();
				return packet.getPacket();
			}
		}
		ChatItem.debug("Checking for " + components.length + " components");
		Arrays.asList(components).forEach(comp -> checkComponent(comp, hover, click, replacement, chat));
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(ComponentSerializer.toString(components)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packet.getPacket();
	}

	private void checkComponent(BaseComponent comp, HoverEvent hover, ClickEvent click, String itemName, Chat chat) {
		if (comp instanceof TextComponent) {
			TextComponent tc = (TextComponent) comp;
			if (ChatManager.containsSeparator(tc.getText())) {
				String oldText = tc.getText();
				ChatItem.debug("Changing text " + oldText + " to " + itemName + ", extra: " + (tc.getExtra() == null ? "-" : tc.getExtra().size()));
				tc.setText(ChatManager.replaceSeparator(chat, oldText, itemName));
				tc.setHoverEvent(hover);
				if (click != null)
					tc.setClickEvent(click);
			} else
				ChatItem.debug("No insert of text without separator: " + tc.getText() + " (legacy: " + tc.toLegacyText() + ")");
		}
		if (comp.getExtra() != null) {
			for (BaseComponent extra : comp.getExtra()) {
				checkComponent(extra, hover, click, itemName, chat);
			}
		}
	}
}
