package me.dadus33.chatitem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.dadus33.chatitem.listeners.holder.CustomInventoryHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class InventoryShower {

	public static void showInventory(Player p, Player cible) {
		Inventory inv = Bukkit.createInventory(new CustomInventoryHolder(), 54, Messages.getMessage("inventory.name", "%cible%", cible.getName()));
		
		for(int i = 0; i < 18; i++)
			inv.setItem(i, ItemUtils.createItem(ItemUtils.getMaterialWithCompatibility("BROWN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), ""));
		
		inv.setItem(2, ItemUtils.createItem(ItemUtils.getMaterialWithCompatibility("EXPERIENCE_BOTTLE", "EXP_BOTTLE"), "", cible.getLevel() == 0 ? 1 : (cible.getLevel() >= 64 ? 64 : cible.getLevel())));
		
		PlayerInventory pi = cible.getInventory();
		inv.setItem(4, pi.getHelmet());
		inv.setItem(5, pi.getChestplate());
		inv.setItem(6, pi.getLeggings());
		inv.setItem(7, pi.getBoots());
		
		for(int i = 0; i < pi.getContents().length && i < (54 - 18); i++)
			inv.setItem(i + 18, pi.getContents()[i]);
		
		p.openInventory(inv);
	}
	
	public static void showEnderchest(Player p, Player cible) {
		Inventory ec = cible.getEnderChest();
		Inventory inv = Bukkit.createInventory(new CustomInventoryHolder(), ec.getSize(), Messages.getMessage("enderchest.name", "%cible%", cible.getName()));
		ItemStack[] items = ec.getContents();
		for(int i = 0; i < ec.getSize(); i++)
			inv.setItem(i, items[i]);
		p.openInventory(inv);
	}
}
