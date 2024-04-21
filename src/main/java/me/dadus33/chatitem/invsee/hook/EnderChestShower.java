package me.dadus33.chatitem.invsee.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.invsee.InvShower;
import me.dadus33.chatitem.listeners.holder.CustomInventoryHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class EnderChestShower extends InvShower {

	private final ItemStack[] items;
	
	public EnderChestShower(Player cible) {
		super("enderchest", cible);
		
		Inventory ec = cible.getEnderChest();
		ItemStack[] tmp = ec.getContents();
		items = new ItemStack[tmp.length];
		for(int i = 0; i < ec.getSize(); i++)
			items[i] = ItemUtils.copyIfExist(tmp[i]);
	}
	
	@Override
	public void open(Player p) {
		Inventory inv = Bukkit.createInventory(new CustomInventoryHolder(), items.length, Messages.getMessage("enderchest.name", "%cible%", name));

		for(int i = 0; i < items.length; i++)
			inv.setItem(i, items[i]);

		p.openInventory(inv);
	}
}
