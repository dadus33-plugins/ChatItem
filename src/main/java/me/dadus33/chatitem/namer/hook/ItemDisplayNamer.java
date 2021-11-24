package me.dadus33.chatitem.namer.hook;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.namer.INamer;
import me.dadus33.chatitem.utils.Storage;

public class ItemDisplayNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.MEDIUM;
	}

	@Override
	public String getName(ItemStack item, Storage storage) {
		if(item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			String name = item.getItemMeta().getDisplayName();
			return storage.COLOR_IF_ALREADY_COLORED ? ChatColor.stripColor(name) : name;
		}
		return null;
	}

}
