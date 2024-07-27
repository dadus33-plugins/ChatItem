package me.dadus33.chatitem.itemnamer.hook;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.itemnamer.INamer;

@SuppressWarnings("deprecation")
public class ItemDisplayNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.MEDIUM;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		String name = ChatItem.getPlatform().getItemDisplayName(item);
		if(name != null && name != "")
			return storage.colorIfColored ? ChatColor.stripColor(name) : name;
		return null;
	}

}
