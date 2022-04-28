package me.dadus33.chatitem.hook;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.willfp.ecoenchants.display.EnchantmentCache;
import com.willfp.ecoenchants.enchantments.util.EnchantChecks;

public class EcoEnchantsSupport {

	public static List<String> getLores(ItemStack item) {
		List<String> lores = new ArrayList<>();
		EnchantChecks.getEnchantsOnItem(item).forEach((en, lvl) -> {
			lores.add(EnchantmentCache.getEntry(en).getNameWithLevel(lvl));
		});
		return lores;
	}
	
}
