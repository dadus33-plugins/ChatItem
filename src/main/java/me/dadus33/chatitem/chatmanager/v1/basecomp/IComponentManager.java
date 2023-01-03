package me.dadus33.chatitem.chatmanager.v1.basecomp;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;

public interface IComponentManager {

	default boolean hasConditions() {
		return true;
	}
	
	String getBaseComponentAsJSON(ChatItemPacket packet);
	
	void writeJson(ChatItemPacket packet, String json);
	
	default @Nullable Chat getChat(String json) {
		try {
			Chat possibleChat = Chat.getFrom(json);
			if(possibleChat != null) // found something with basic search
				return possibleChat;
			boolean found = false;
			String id = "";
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if(!jsonObj.has("extra"))
				return null;
			for(JsonElement element : jsonObj.get("extra").getAsJsonArray()) {
				if(element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if(withObj.has("extra")) {
						for(JsonElement extra : withObj.get("extra").getAsJsonArray()) {
							if(extra.isJsonObject()) {
								JsonObject extraObj = extra.getAsJsonObject();
								if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive()) {
									for(char c : ChatManager.fixSeparator(extraObj.get("text").getAsString()).toCharArray()) {
										if(c == ChatManager.SEPARATOR)
											found = true;
										else if(c == ChatManager.SEPARATOR_END) {
											if(Utils.isInteger(id))
												return Chat.getChat(Integer.parseInt(id)).orElse(null);
											ChatItem.debug("The id " + id + " is not a number for extra text.");
											return null;
										} else if(found)
											id += c;
									}
								}
							}
						}
					} else if(withObj.has("text")) {
						for(char c : ChatManager.fixSeparator(withObj.get("text").getAsString()).toCharArray()) {
							if(c == ChatManager.SEPARATOR)
								found = true;
							else if(c == ChatManager.SEPARATOR_END) {
								if(Utils.isInteger(id))
									return Chat.getChat(Integer.parseInt(id)).orElse(null);
								ChatItem.debug("The id " + id + " is not a number for text.");
								return null;
							} else if(found)
								id += c;
						}
					}
				} // ignoring all others because it should not appear
			}
			if(Utils.isInteger(id)) {
				return Chat.getChat(Integer.parseInt(id)).orElse(null);
			} else
				ChatItem.debug("The id " + id + " is not a number.");
		} catch (Exception e) {
			e.printStackTrace();
		} // not JSON
		return null;
	}

	default Object manageItem(Player p, Chat chat, ChatItemPacket packet, ItemStack item, Storage c) throws Exception {
		String message = JSONManipulator.getInstance().parse(getBaseComponentAsJSON(packet), item, ChatManager.styleItem(chat.getPlayer(), item, c), ItemPlayer.getPlayer(p).getProtocolVersion());
		if (message != null) {
			ChatItem.debug("(v1) Writing message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}
	
	default Object manageEmpty(Player p, Chat chat, ChatItemPacket packet, Storage c) {
		String message = JSONManipulator.getInstance().parseEmpty(getBaseComponentAsJSON(packet), c.HAND_NAME, c.HAND_TOOLTIP, chat.getPlayer());
		if (message != null) {
			ChatItem.debug("(v1) Writing empty message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}
}
