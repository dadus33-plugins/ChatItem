package me.dadus33.chatitem.invsee.hook;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.dadus33.chatitem.invsee.InvShower;
import me.dadus33.chatitem.listeners.holder.CustomInventoryHolder;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;

public class PlayerInventoryShower extends InvShower {

	private final HashMap<Integer, ItemStack> items = new HashMap<>();	
	private final int level;
	
	public PlayerInventoryShower(Player cible) {
		super("inventory", cible);
		this.level = cible.getLevel();
		
		PlayerInventory pi = cible.getInventory();
		items.put(4, ItemUtils.copyIfExist(pi.getHelmet()));
		items.put(5, ItemUtils.copyIfExist(pi.getChestplate()));
		items.put(6, ItemUtils.copyIfExist(pi.getLeggings()));
		items.put(7, ItemUtils.copyIfExist(pi.getBoots()));
		
		for(int i = 0; i < pi.getContents().length && i < (54 - 18); i++)
			items.put(i + 18, ItemUtils.copyIfExist(pi.getContents()[i]));
	}
	
	@Override
	public void open(Player p) {
		Inventory inv = Bukkit.createInventory(new CustomInventoryHolder(), 54, Messages.getMessage("inventory.name", "%cible%", name));
		
		for(int i = 0; i < 18; i++)
			inv.setItem(i, ItemUtils.createItem(ItemUtils.getMaterialWithCompatibility("BROWN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), ""));
		
		inv.setItem(2, new ItemStack(ItemUtils.getMaterialWithCompatibility("EXPERIENCE_BOTTLE", "EXP_BOTTLE"), level == 0 ? 1 : (level >= 64 ? 64 : level)));
		
		items.forEach(inv::setItem);

		p.openInventory(inv);
	}
	
}
