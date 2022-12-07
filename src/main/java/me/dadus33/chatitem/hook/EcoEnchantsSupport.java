package me.dadus33.chatitem.hook;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class EcoEnchantsSupport {

	public static List<String> getLores(ItemStack item) {
		List<String> lores = new ArrayList<>();
		/*com.willfp.ecoenchants.
		EnchantChecks.getEnchantsOnItem(item).forEach((en, lvl) -> {
			lores.add(EnchantmentCache.getEntry(en).getNameWithLevel(lvl));
		});*/
		return lores;
	}
	
}
