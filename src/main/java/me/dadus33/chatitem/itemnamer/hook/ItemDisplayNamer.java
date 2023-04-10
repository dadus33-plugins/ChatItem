package me.dadus33.chatitem.itemnamer.hook;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.itemnamer.INamer;

public class ItemDisplayNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.MEDIUM;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			String name = item.getItemMeta().getDisplayName();
			return storage.colorIfColored ? ChatColor.stripColor(name) : name;
		}
		return null;
	}

}
