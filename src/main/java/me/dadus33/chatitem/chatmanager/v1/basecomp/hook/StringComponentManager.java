package me.dadus33.chatitem.chatmanager.v1.basecomp.hook;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
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
	public Object manageItem(Player p, ItemPlayer itemPlayer, ChatItemPacket packet, ItemStack item, Storage c)
			throws Exception {
		String itemName = ChatManager.getNameOfItem(itemPlayer.getPlayer(), item, c);
		return manage(p, itemPlayer, packet, itemName, Utils.createItemHover(item));
	}

	@Override
	public Object manageEmpty(Player p, ItemPlayer itemPlayer, ChatItemPacket packet, Storage c) {
		ComponentBuilder builder = new ComponentBuilder("");
		c.HAND_TOOLTIP.forEach(s -> builder.append(s));
		Player sender = itemPlayer.getPlayer();
		String handName = c.HAND_NAME.replace("{name}", sender.getName()).replace("{display-name}",
				sender.getDisplayName());
		return manage(p, itemPlayer, packet, handName, Utils.createTextHover(builder.create()));
	}

	private Object manage(Player p, ItemPlayer itemPlayer, ChatItemPacket packet, String replacement,
			HoverEvent hover) {
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
		BaseComponent[] components = ComponentSerializer.parse(json);
		ChatItem.debug("Checking for json " + json + ", with " + components.length + " components");
		Arrays.asList(components)
				.forEach(comp -> checkComponent(comp, hover, replacement, itemPlayer.getPlayer().getName()));
		try {
			packet.setPacket(PacketEditingChatManager.createSystemChatPacket(ComponentSerializer.toString(components)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packet.getPacket();
	}

	private void checkComponent(BaseComponent comp, HoverEvent hover, String itemName, String playerName) {
		if (ChatManager.containsSeparator(comp.getInsertion())) {
			comp.setInsertion(ChatManager.replaceSeparator(comp.getInsertion(), itemName, playerName));
			comp.setHoverEvent(hover);
			ChatItem.debug("Changed " + comp.getInsertion());
		} else if (comp instanceof TextComponent) {
			TextComponent tc = (TextComponent) comp;
			if (ChatManager.containsSeparator(tc.getText())) {
				tc.setText(ChatManager.replaceSeparator(tc.getText(), itemName, playerName));
				tc.setHoverEvent(hover);
				ChatItem.debug("Changed text " + tc.getText());
			} else
				ChatItem.debug("No insert of text without separator: " + tc.getText() + " (legacy: " + tc.toLegacyText()
						+ ")");
		} else
			ChatItem.debug(
					"No insert without separator: " + comp.getInsertion() + " (legacy: " + comp.toLegacyText() + ")");
		if (comp.getExtra() != null) {
			for (BaseComponent extra : comp.getExtra()) {
				checkComponent(extra, hover, itemName, playerName);
			}
		}
	}
}
