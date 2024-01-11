package me.dadus33.chatitem.chatmanager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Version;

public class HandItem {

	@SuppressWarnings("deprecation")
	public static ItemStack getBetterItem(Player p, ItemSlot slot) {
		switch (slot) {
		case HAND:
			if(Version.getVersion().isNewerOrEquals(Version.V1_9)) {
				ItemStack main = p.getInventory().getItemInMainHand();
				if(!ItemUtils.isEmpty(main))
					return main;
				return p.getInventory().getItemInOffHand();
			}
			return p.getItemInHand();
		case HELMET:
			return p.getInventory().getHelmet();
		case CHESTPLATE:
			return p.getInventory().getChestplate();
		case LEGGINGS:
			return p.getInventory().getLeggings();
		case BOOTS:
			return p.getInventory().getBoots();
		case ENDERCHEST:
			break;
		case INVENTORY:
			break;
		}
		return null; // should never happen
	}
}
