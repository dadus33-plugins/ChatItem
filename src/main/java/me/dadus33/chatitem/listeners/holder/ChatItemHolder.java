package me.dadus33.chatitem.listeners.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class ChatItemHolder implements InventoryHolder {
    
    @Override
    public Inventory getInventory() {
        return null;
    }
}
