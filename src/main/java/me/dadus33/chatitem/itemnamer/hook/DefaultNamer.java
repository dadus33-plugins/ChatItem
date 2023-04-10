package me.dadus33.chatitem.itemnamer.hook;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.itemnamer.INamer;

public class DefaultNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.MINOR;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		Material m = item.getType();
		return m.equals(Material.TNT) ? "TNT" : WordUtils.capitalize(m.name().replaceAll("_", " ").toLowerCase());
	}
}
