package me.dadus33.chatitem.hook.ecoenchants;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.willfp.eco.core.display.DisplayProperties;
import com.willfp.ecoenchants.EcoEnchantsPlugin;
import com.willfp.ecoenchants.display.EnchantDisplay;

public class EcoEnchantsV10Support {

	private static EnchantDisplay enchantDisplay;

	public static EnchantDisplay getEnchantDisplay() {
		if (enchantDisplay == null) {
			try {
				enchantDisplay = EnchantDisplay.class.getConstructor(EcoEnchantsPlugin.class).newInstance(Bukkit.getPluginManager().getPlugin("EcoEnchants"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return enchantDisplay;
	}

	public static void display(ItemStack item) {
		try {
			DisplayProperties display = DisplayProperties.class.getDeclaredConstructor(boolean.class, boolean.class, ItemStack.class).newInstance(false, false, item);
			getEnchantDisplay().display(item, (Player) null, display, new Object[] { false });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}