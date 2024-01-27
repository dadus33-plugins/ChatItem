package me.dadus33.chatitem.hook.ecoenchants;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;

public class EcoEnchantsSupport {

	private static int supportedVersion = 0;
	
	public static boolean hasSupport() {
		return supportedVersion > 0;
	}
	
	public static boolean load() {
		String ver = Bukkit.getPluginManager().getPlugin("EcoEnchants").getDescription().getVersion();
		if(ver.startsWith("8."))
			supportedVersion = 8;
		else if(ver.startsWith("10."))
			supportedVersion = 10;
		else if(ver.startsWith("11."))
			supportedVersion = 11;
		else if(ver.startsWith("12."))
			supportedVersion = 12;
		else {
			ChatItem.getInstance().getLogger().warning("Failed to find support version for EcoEnchants " + ver);
			return false;
		}
		return true;
	}
	
	public static ItemStack manageItem(ItemStack item) {
		if(supportedVersion == 8) {
			List<String> addLore = EcoEnchantsV8Support.getLores(item);
			if (!addLore.isEmpty()) {
				ItemMeta meta = item.getItemMeta();
				List<String> lores = meta.hasLore() ? meta.getLore() : new ArrayList<>();
				for (int i = 0; i < addLore.size(); i++)
					lores.add(i, addLore.get(i));

				meta.setLore(lores);
				item.setItemMeta(meta);
				ChatItem.debug("Added " + addLore.size() + " lores from EcoEnchants");
			} else
				ChatItem.debug("No lore to add from EcoEnchants");
		} else if(supportedVersion >= 10) {
			EcoEnchantsV10Support.display(item);
		}
		return item;
	}
}
