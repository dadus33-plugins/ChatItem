package me.dadus33.chatitem;

import java.util.List;

import me.dadus33.chatitem.chatmanager.ChatManager;

public enum ItemSlot {

	HAND(true, ""), HELMET(false, "helmet"), CHESTPLATE(false, "chestplate"), LEGGINGS(false, "leggings"), BOOTS(false, "boots");
	
	private final boolean basic;
	private final String key;
	
	private ItemSlot(boolean basic, String key) {
		this.basic = basic;
		this.key = key;
	}
	
	public boolean isBasic() {
		return basic;
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
