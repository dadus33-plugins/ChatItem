package me.dadus33.chatitem.chatmanager.v1.basecomp;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
		for (String rep : c.PLACEHOLDERS) {
			if (json.contains(rep)) {
				return rep;
			}
		}
		return null;
	}
	
	default @Nullable String getNameFromMessage(String json, String toReplace) {
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
		return json.replace(toReplace + foundedPlayer.getName(), Character.toString(ChatManager.SEPARATOR));
	}
}
