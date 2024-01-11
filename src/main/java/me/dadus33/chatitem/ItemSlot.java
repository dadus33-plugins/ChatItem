package me.dadus33.chatitem;

import java.util.List;

import me.dadus33.chatitem.chatmanager.ChatManager;

public enum ItemSlot {

	HAND(true, "", false),
	HELMET(false, "helmet", false),
	CHESTPLATE(false, "chestplate", false),
	LEGGINGS(false, "leggings", false),
	BOOTS(false, "boots", false),
	INVENTORY(false, "inventory", true),
	ENDERCHEST(false, "enderchest", true);
	
	private final boolean basic, command;
	private final String key;
	
	private ItemSlot(boolean basic, String key, boolean command) {
		this.basic = basic;
		this.command = command;
		this.key = key;
	}
	
	public boolean isBasic() {
		return basic;
	}
	
	public boolean isCommand() {
		return command;
	}
	
	public List<String> getPlaceholders() {
		return ChatItem.getInstance().getConfig().getStringList(basic ? "general.placeholders" : "general.other-placeholders." + key + ".keys");
	}
	
	public boolean isEnabled() {
		return basic || ChatItem.getInstance().getConfig().getBoolean("general.other-placeholders." + key + ".enabled", true);
	}
	
	public boolean isDenyIfNoItem() {
		return ChatItem.getInstance().getConfig().getBoolean("general" + (basic ? "" : ".other-placeholders." + key) + ".deny-if-no-item", true);
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
}
