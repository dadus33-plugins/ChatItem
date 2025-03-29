package me.dadus33.chatitem.invsee.hook;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.invsee.InvShower;
import me.dadus33.chatitem.listeners.holder.CustomInventoryHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class OneItemShower extends InvShower {

	private final ItemStack item;	
	
	public OneItemShower(Player cible, ItemStack item) {
		super("one_item", cible);
		
		this.item = item.clone();
	}
	
	@Override
	public void open(Player p) {
		Inventory inv = ChatItem.getPlatform().createInventory(new CustomInventoryHolder(), 9, Messages.getMessage("inventory.name", "%cible%", name));
		
		for(int i = 0; i < 9; i++)
			inv.setItem(i, ItemUtils.ITEM_EMPTY_BROWN);
		
		inv.setItem(4, item);

		p.openInventory(inv);
	}
	
}
