package me.dadus33.chatitem.itemnamer.hook;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.meowj.langutils.lang.LanguageHelper;

import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.itemnamer.INamer;

public class LangUtilsNamer implements INamer {

	@Override
	public Priority getPriority() {
		return Priority.IMPORTANT;
	}

	@Override
	public String getName(Player p, ItemStack item, Storage storage) {
		return LanguageHelper.getItemName(item, p);
	}

}
