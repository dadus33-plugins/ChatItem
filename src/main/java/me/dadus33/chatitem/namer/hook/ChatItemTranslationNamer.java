package me.dadus33.chatitem.namer.hook;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.namer.INamer;
import me.dadus33.chatitem.utils.Storage;

public class ChatItemTranslationNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.SMALL;
	}

	@Override
	public String getName(ItemStack item, Storage storage) {
		HashMap<Short, String> translationSection = storage.TRANSLATIONS.get(item.getType().name());
		if (translationSection != null) {
			@SuppressWarnings("deprecation")
			String translated = translationSection.get(item.getDurability());
			if (translated != null) {
				return translated;
			}
		}
		return null;
	}

}
