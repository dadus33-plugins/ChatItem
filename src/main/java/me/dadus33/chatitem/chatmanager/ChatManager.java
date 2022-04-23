package me.dadus33.chatitem.chatmanager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.utils.Storage;

public abstract class ChatManager {

	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	public final static char SEPARATOR = ((char) 0x0007);
	public final static String SEPARATOR_STR = "\\u0007";
	
	protected Storage s;
	
	public ChatManager() {
		
	}
	
	public abstract String getName();
	
	public abstract String getId();
	
	public Storage getStorage() {
		return s;
	}
	
	public void load(ChatItem pl, Storage s) {
		this.s = s;
	}
	
	public abstract void unload(ChatItem pl);

	public static String styleItem(Player p, ItemStack item, Storage c) {
		String replacer = c.NAME_FORMAT;
		String amount = c.AMOUNT_FORMAT;
		boolean dname = item.hasItemMeta() && item.getItemMeta().hasDisplayName();
		if (item.getAmount() == 1) {
			if (c.FORCE_ADD_AMOUNT) {
				amount = amount.replace(TIMES, "1");
				replacer = replacer.replace(AMOUNT, amount);
			} else {
				replacer = replacer.replace(AMOUNT, "");
			}
		} else {
			amount = amount.replace(TIMES, String.valueOf(item.getAmount()));
			replacer = replacer.replace(AMOUNT, amount);
		}
		if (dname) {
			String trp = item.getItemMeta().getDisplayName();
			if (c.COLOR_IF_ALREADY_COLORED) {
				replacer = replacer.replace(NAME, ChatColor.stripColor(trp));
			} else {
				replacer = replacer.replace(NAME, trp);
			}
		} else {
			replacer = replacer.replace(NAME, NamerManager.getName(p, item, c));
		}
		return replacer;
	}
}
