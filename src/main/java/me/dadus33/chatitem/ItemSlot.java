package me.dadus33.chatitem;

import java.util.List;

import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.utils.Colors;

public enum ItemSlot {

	HAND("", false),
	HELMET("helmet", false),
	CHESTPLATE("chestplate", false),
	LEGGINGS("leggings", false),
	BOOTS("boots", false),
	INVENTORY("inventory", true),
	ENDERCHEST("enderchest", true);
	
	private final boolean command;
	private final String key;
	
	private ItemSlot(String key, boolean command) {
		this.command = command;
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public boolean isBasic() {
		return key.equalsIgnoreCase("");
	}
	
	public boolean isCommand() {
		return command;
	}
	
	public List<String> getPlaceholders() {
		return ChatItem.getInstance().getConfig().getStringList(isBasic() ? "general.placeholders" : "general.other-placeholders." + key + ".keys");
	}
	
	public boolean isEnabled() {
		return isBasic() || ChatItem.getInstance().getConfig().getBoolean("general.other-placeholders." + key + ".enabled", true);
	}
	
	public boolean isDenyIfNoItem() {
		return ChatItem.getInstance().getConfig().getBoolean("general" + (isBasic() ? "" : ".other-placeholders." + key) + ".deny-if-no-item", true);
	}
	 
	public String getShowMessage() {
		return Colors.color(ChatItem.getInstance().getConfig().getString("general" + (isBasic() ? "" : ".other-placeholders." + key) + ".show", ""));
	}
	
	public boolean hasPlaceholders(String message) {
		for (String rep : getPlaceholders()) {
			if (message.contains(rep)) {
				return true;
			}
		}
		return false;
	}
	
	public String replacePlaceholdersToSeparator(String message) {
		for(String rep : getPlaceholders())
			message = message.replace(rep, Character.toString(ChatManager.SEPARATOR));
		return message;
	}
	
	public static ItemSlot getItemSlotFromMessage(String message) {
		for(ItemSlot slot : ItemSlot.values()) {
			if(slot.isEnabled() && slot.hasPlaceholders(message)) {
				return slot;
			}
		}
		return null;
	}
	
	public static ItemSlot getItemSlotByKey(String key) {
		for(ItemSlot slot : ItemSlot.values()) {
			if(slot.isEnabled() && slot.getKey().equalsIgnoreCase(key)) {
				return slot;
			}
		}
		return null;
	}
}
