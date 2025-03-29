package me.dadus33.chatitem.chatmanager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ItemSlot;

public class ChatAction {

	private boolean hasItem;
	private ItemSlot slot;
	private String command;
	private ItemStack item;
	private Player origin;
	
	public ChatAction(ItemSlot slot, Player origin, ItemStack item, String command) {
		this.slot = slot;
		this.item = item == null ? new ItemStack(Material.AIR) : item;
		this.command = command;
		this.origin = origin;
		this.hasItem = true;
	}
	
	public ChatAction(ItemSlot slot, Player origin, String command) {
		this.slot = slot;
		this.command = command;
		this.origin = origin;
		this.hasItem = false;
	}
	
	public Player getOrigin() {
		return origin;
	}
	
	public ItemSlot getSlot() {
		return slot;
	}
	
	public boolean hasItem() {
		return hasItem;
	}
	
	public String getCommand() {
		return command;
	}
	
	public boolean hasCommand() {
		return command != null && command != "";
	}
	
	public ItemStack getItem() {
		return item == null ? new ItemStack(Material.AIR) : item;
	}
	
	@Override
	public String toString() {
		return "ChatAction[slot=" + slot + ",command=" + command + ",item=" + item + ",origin=" + origin + "]";
	}
}
