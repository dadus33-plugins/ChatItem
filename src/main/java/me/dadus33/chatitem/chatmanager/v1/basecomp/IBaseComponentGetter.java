package me.dadus33.chatitem.chatmanager.v1.basecomp;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Storage;

public interface IBaseComponentGetter {

	default boolean hasConditions() {
		return true;
	}
	
	String getBaseComponentAsJSON(ChatItemPacket packet);
	
	void writeJson(ChatItemPacket packet, String json);
	
	default @Nullable String getNameFromMessage(String json, String toReplace) {
		String checkedName = getNameFromSpecificMessage(json, toReplace);
		if(checkedName != null)
			return checkedName;
		try {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if(!jsonObj.has("extra"))
				return null;
			String text = "";
			for(JsonElement element : jsonObj.get("extra").getAsJsonArray()) {
				if(element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if(withObj.has("extra")) {
						for(JsonElement extra : withObj.get("extra").getAsJsonArray()) {
							if(extra.isJsonObject()) {
								JsonObject extraObj = extra.getAsJsonObject();
								if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive())
									text += extraObj.get("text").getAsString();
							}
						}
					} else if(withObj.has("text")) {
						text += withObj.get("text").getAsString();
					}
				} // ignoring all others because it should not appear
			}
			String possibleName = getNameFromSpecificMessage(text, toReplace);
			if(possibleName != null)
				return possibleName;
		} catch (Exception e) {} // not JSON
		return null;
	}
	
	default @Nullable String getNameFromSpecificMessage(String json, String toReplace) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			String name = toReplace + p.getName();
			if (json.contains(name))
				return p.getName();
		}
		return null;
	}
	
	/**
	 * Use actual base component getter to remove the player and the name.<br>
	 * This is for special component which split message into sub string
	 * 
	 * @param json the global json
	 * @param toReplace the string which contains the placeholder
	 * @param foundedPlayer the player which has the name in message
	 * @return the replaced json
	 */
	default String removePlaceholdersAndName(String json, String toReplace, Player foundedPlayer) {
		String remove = toReplace + foundedPlayer.getName();
		boolean firstRemove = true;
		try {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if(!jsonObj.has("extra")) {
				ChatItem.debug("[IBase] No extra in json");
				return json.replace(remove, Character.toString(ChatManager.SEPARATOR));
			}
			JsonArray extraArray = jsonObj.get("extra").getAsJsonArray();
			for(int i = 0; i < extraArray.size(); i++) {
				JsonElement element = extraArray.get(i);
				if(element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if(withObj.has("extra")) {
						for(JsonElement extra : withObj.get("extra").getAsJsonArray()) {
							if(extra.isJsonObject()) {
								JsonObject extraObj = extra.getAsJsonObject();
								if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive()) {
									String s = extraObj.get("text").getAsString();
									if(remove.startsWith(s)) {
										extraObj.addProperty("text", firstRemove ? Character.toString(ChatManager.SEPARATOR) : "");
										firstRemove = false;
										remove = remove.substring(s.length());
										if(remove.isEmpty())
											return jsonObj.toString();
									}
								}
							}
						}
					} else if(withObj.has("text")) {
						String s = withObj.get("text").getAsString();
						if(remove.startsWith(s)) {
							withObj.addProperty("text", firstRemove ? Character.toString(ChatManager.SEPARATOR) : "");
							firstRemove = false;
							remove = remove.substring(s.length());
							if(remove.isEmpty())
								return jsonObj.toString();
						}
					}
				} else if(element.isJsonPrimitive()) {
					String s = element.getAsString();
					if(s.contains(remove)) {
						extraArray.set(i, new JsonPrimitive(s.replace(remove, Character.toString(ChatManager.SEPARATOR))));
						return jsonObj.toString();
					}
				}
				
				// ignoring all others because it should not appear
			}
			ChatItem.debug("[IBase] Remove not fully managed: " + remove + " > " + jsonObj.toString());
			return jsonObj.toString();
		} catch (Exception e) {} // not JSON
		return json.replace(remove, Character.toString(ChatManager.SEPARATOR));
	}

	default Object manageItem(Player p, ItemPlayer itemPlayer, ChatItemPacket packet, ItemStack item, Storage c) throws Exception {
		String message = JSONManipulator.getInstance().parse(getBaseComponentAsJSON(packet), item, ChatManager.styleItem(itemPlayer.getPlayer(), item, c), ItemPlayer.getPlayer(p).getProtocolVersion());
		if (message != null) {
			ChatItem.debug("(v1) Writing message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}
	
	default Object manageEmpty(Player p, ItemPlayer itemPlayer, ChatItemPacket packet, Storage c) {
		String message = JSONManipulator.getInstance().parseEmpty(getBaseComponentAsJSON(packet), c.HAND_NAME, c.HAND_TOOLTIP, itemPlayer.getPlayer());
		if (message != null) {
			ChatItem.debug("(v1) Writing empty message: " + message);
			writeJson(packet, message);
		}
		return packet.getPacket();
	}
}
