package me.dadus33.chatitem.chatmanager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Version;

public class HandItem {

	@SuppressWarnings("deprecation")
	public static ItemStack getBetterItem(Player p) {
		if(Version.getVersion().isNewerOrEquals(Version.V1_9)) {
			ItemStack main = p.getInventory().getItemInMainHand();
			if(!ItemUtils.isEmpty(main))
				return main;
			ItemStack off = p.getInventory().getItemInOffHand();
			return ItemUtils.isEmpty(off) ? main : off;
		}
		return p.getItemInHand();
	}
}
