package me.dadus33.chatitem.chatmanager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ItemSlot;

public class ChatAction {

	private boolean isItem;
	private ItemSlot slot;
	private String command;
	private ItemStack item;
	private Player origin;
	
	public ChatAction(ItemSlot slot, Player origin, ItemStack item) {
		this.slot = slot;
		this.item = item;
		this.origin = origin;
		this.isItem = true;
	}
	
	public ChatAction(ItemSlot slot, Player origin, String command) {
		this.slot = slot;
		this.command = command;
		this.origin = origin;
		this.isItem = false;
	}
	
	public Player getOrigin() {
		return origin;
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
