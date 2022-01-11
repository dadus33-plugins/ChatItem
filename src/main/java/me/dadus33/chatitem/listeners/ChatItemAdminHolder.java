package me.dadus33.chatitem.listeners;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ChatItemAdminHolder implements InventoryHolder {
    
	public final HashMap<Integer, String> keyBySlot = new HashMap<>();
	
    @Override
    public Inventory getInventory() {
        return null;
    }

}
