package me.dadus33.chatitem.itemnamer.hook;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.itemnamer.INamer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PaperNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.IMPORTANT;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		return item.getItemMeta().hasDisplayName() ? LegacyComponentSerializer.legacySection().serialize(item.displayName()) : null;
	}
}
