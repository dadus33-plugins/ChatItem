package me.dadus33.chatitem.listeners;

import static me.dadus33.chatitem.utils.ItemUtils.createItem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class InventoryListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player) || !(e.getClickedInventory().getHolder() instanceof ChatItemAdminHolder))
			return;
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if(item.getType().equals(ItemUtils.MATERIAL_CLOSE)) {
			p.closeInventory();
			return;
		}
	}
	
	public static void open(Player p) {
		Inventory inv = Bukkit.createInventory(new ChatItemAdminHolder(), 27, Messages.getMessage("admin-inv.name"));
		
		
		inv.setItem(inv.getSize() - 1, createItem(ItemUtils.MATERIAL_CLOSE, Messages.getMessage("admin-inv.close")));
		p.openInventory(inv);
	}
}
