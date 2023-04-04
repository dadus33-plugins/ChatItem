package me.dadus33.chatitem.listeners.holder;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AdminHolder implements InventoryHolder {
    
	public final HashMap<Integer, String> keyBySlot = new HashMap<>();
	
    @Override
    public Inventory getInventory() {
        return null;
    }

}
