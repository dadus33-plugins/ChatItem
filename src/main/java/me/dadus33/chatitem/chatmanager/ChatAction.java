package me.dadus33.chatitem.chatmanager;

import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ItemSlot;

public class ChatAction {

	private boolean isItem;
	private ItemSlot slot;
	private String command;
	private ItemStack item;
	
	public ChatAction(ItemSlot slot, ItemStack item) {
		this.slot = slot;
		this.item = item;
		this.isItem = true;
	}
	
	public ChatAction(ItemSlot slot, String command) {
		this.slot = slot;
		this.command = command;
		this.isItem = false;
	}
	
	public ItemSlot getSlot() {
		return slot;
	}
	
	public boolean isItem() {
		return isItem;
	}
	
	public String getCommand() {
		return command;
	}
	
	public ItemStack getItem() {
		return item;
	}
}
