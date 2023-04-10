package me.dadus33.chatitem.itemnamer.hook;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.Translation;
import me.dadus33.chatitem.itemnamer.INamer;

public class ChatItemTranslationNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.SMALL;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		return Translation.get(item);
	}

}
