package me.dadus33.chatitem.chatmanager.v1.basecomp;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.utils.Storage;

public interface IBaseComponentGetter {

	default boolean hasConditions() {
		return true;
	}
	
	String getBaseComponentAsJSON(ChatItemPacket packet);
	
	void writeJson(ChatItemPacket packet, String json);
	
	/**
	 * Use actual base component getter to check if the given json has placeholders
	 * 
	 * @param c the storage config
	 * @param json the json
	 * @return the founded placeholder or null
	 */
	default @Nullable String hasPlaceholders(Storage c, String json) {
		if(json == null || json.isEmpty())
			return null;
		String placeholder = hasPlaceholdersSpecificMessage(c, json);
		if(placeholder != null)
			return placeholder;
		try {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if(!jsonObj.has("extra"))
				return null;
			for(JsonElement element : jsonObj.get("extra").getAsJsonArray()) {
				if(element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if(withObj.has("extra")) {
						String text = "";
						for(JsonElement extra : withObj.get("extra").getAsJsonArray()) {
							if(extra.isJsonObject()) {
								JsonObject extraObj = extra.getAsJsonObject();
								if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive())
									text += extraObj.get("text").getAsString();
							}
						}
						placeholder = hasPlaceholdersSpecificMessage(c, text);
						if(placeholder != null)
							return placeholder;
					} else if(withObj.has("text")) {
						placeholder = hasPlaceholdersSpecificMessage(c, withObj.get("text").getAsString());
						if(placeholder != null)
							return placeholder;
					}
				} // ignoring all others because it should not appear
			}
		} catch (Exception e) {} // not JSON
		return null;
	}
	
	default @Nullable String hasPlaceholdersSpecificMessage(Storage c, String json) {
		if(json == null || json.isEmpty())
			return null;
		for (String rep : c.PLACEHOLDERS) {
			if (json.contains(rep)) {
				return rep;
			}
		}
		return null;
	}
	
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
		try {
			JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
			if(!jsonObj.has("extra"))
				return null;
			String remove = toReplace + foundedPlayer.getName();
			for(JsonElement element : jsonObj.get("extra").getAsJsonArray()) {
				if(element.isJsonObject()) {
					JsonObject withObj = element.getAsJsonObject();
					if(withObj.has("extra")) {
						for(JsonElement extra : withObj.get("extra").getAsJsonArray()) {
							if(extra.isJsonObject()) {
								JsonObject extraObj = extra.getAsJsonObject();
								if(extraObj.has("text") && extraObj.get("text").isJsonPrimitive()) {
									String s = extraObj.get("text").getAsString();
									if(remove.startsWith(s)) {
										extraObj.addProperty("text", "");
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
							withObj.addProperty("text", "");
							remove = remove.substring(s.length());
							if(remove.isEmpty())
								return jsonObj.toString();
						}
					}
				} // ignoring all others because it should not appear
			}
			return jsonObj.toString();
		} catch (Exception e) {} // not JSON
		return json.replace(toReplace + foundedPlayer.getName(), Character.toString(ChatManager.SEPARATOR));
	}
}
