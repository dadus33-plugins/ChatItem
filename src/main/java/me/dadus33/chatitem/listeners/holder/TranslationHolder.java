package me.dadus33.chatitem.listeners.holder;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class TranslationHolder implements InventoryHolder {
    
	public final HashMap<Integer, String> langBySlot = new HashMap<>();
	private final int page;
	
	public TranslationHolder(int page) {
		this.page = page;
	}
	
	public int getPage() {
		return page;
	}
	
    @Override
    public Inventory getInventory() {
        return null;
    }
}
