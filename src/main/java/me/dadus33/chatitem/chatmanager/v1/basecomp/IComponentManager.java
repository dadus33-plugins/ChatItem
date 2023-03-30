package me.dadus33.chatitem.chatmanager.v1.basecomp;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
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
		json = ChatManager.fixSeparator(json);
		try {
			Chat possibleChat = Chat.getFrom(json);
			if (possibleChat != null) // found something with basic search
				return possibleChat;
			Chat chat = new Searching(json).search();
			if (chat == null)
				ChatItem.debug("Failed to find chat for JSON " + json);
			return chat;
		} catch (Exception e) {
			e.printStackTrace();
		} // not JSON
		return null;
	}

	default Object manageItem(Player p, Chat chat, ChatItemPacket packet, ItemStack item, Storage c) throws Exception {
		String message = JSONManipulator.getInstance().parse(chat, getBaseComponentAsJSON(packet), item, ChatManager.styleItem(chat.getPlayer(), item, c),
				ItemPlayer.getPlayer(p).getProtocolVersion());
		if (message != null) {
			ChatItem.debug("(v1) Writing message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}

	default Object manageEmpty(Player p, Chat chat, ChatItemPacket packet, Storage c) {
		String message = JSONManipulator.getInstance().parseEmpty(getBaseComponentAsJSON(packet), c.handName, c.tooltipHand, chat.getPlayer());
		if (message != null) {
			ChatItem.debug("(v1) Writing empty message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}

	public static class Searching {

		private boolean found = false;
		private String id = "", json;

		public Searching(String json) {
			this.json = json;
		}

		private Chat search() {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if (jsonObj.has("extra")) {
				if (searchInExtra(jsonObj.getAsJsonArray("extra"))) {
					return getWithId();
				}
			}
			if (jsonObj.has("text")) {
				JsonElement text = jsonObj.get("text");
				if(text.isJsonObject() && searchInObject(text.getAsJsonObject()))
					return getWithId();
				else if(text.isJsonArray() && searchInExtra(text.getAsJsonArray()))
					return getWithId();
				else if(text.isJsonPrimitive() && searchInString(text.getAsString()))
					return getWithId();
			}
			return null;
		}

		private Chat getWithId() {
			return id != "" && Utils.isInteger(id) ? Chat.getChat(Integer.parseInt(id)).orElse(null) : null;
		}

		private boolean searchInExtra(JsonArray json) {
			for (JsonElement element : json) {
				if (element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if (withObj.has("extra") && searchInExtra(withObj.getAsJsonArray("extra")))
						return true;
					else if (withObj.has("text") && searchInObject(withObj))
						return true;
				}
			}
			return false;
		}

		private boolean searchInObject(JsonObject json) {
			return json.has("text") && json.get("text").isJsonPrimitive() && searchInString(json.get("text").getAsString());
		}

		private boolean searchInString(String s) {
			for (char c : ChatManager.fixSeparator(s).toCharArray()) {
				if (c == ChatManager.SEPARATOR)
					found = true;
				else if (c == ChatManager.SEPARATOR_END)
					return true;
				else if (found)
					id += c;
			}
			return false;
		}
	}
}
