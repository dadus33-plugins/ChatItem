package me.dadus33.chatitem.chatmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.hook.EcoEnchantsSupport;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.utils.ColorManager;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Storage;

public abstract class ChatManager {

	public final static HashMap<UUID, Long> COOLDOWNS = new HashMap<>();
	public final static HashMap<UUID, Long> LAST_INFO_MESSAGE = new HashMap<>();
	public final static String NAME = "{name}";
	public final static String AMOUNT = "{amount}";
	public final static String TIMES = "{times}";
	public final static String LEFT = "{remaining}";
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
	
	/**
	 * Get the item in hand as usable one.<br>
	 * Will include, in lore, all informations from other plugins.
	 * 
	 * @param p the player
	 * @return the usable item
	 */
	public static ItemStack getUsableItem(Player p) {
		ItemStack item = HandItem.getBetterItem(p).clone();
		if(ChatItem.ecoEnchantsSupport) {
			List<String> addLore = EcoEnchantsSupport.getLores(item);
			if(!addLore.isEmpty()) {
				ItemMeta meta = item.getItemMeta();
				List<String> lores = meta.hasLore() ? meta.getLore() : new ArrayList<>();
				for(int i = 0; i < addLore.size(); i++)
					lores.add(i, addLore.get(i));
				
				meta.setLore(lores);
				item.setItemMeta(meta);
			}
		}
		return item;
	}

	/**
	 * Get the name of item according to player & config<br>
	 * Prefer use {@link #getNameOfItem(Player, ItemStack, Storage)} if you want take in count the empty item
	 *  
	 * @param p the player
	 * @param item the item
	 * @param c the config
	 * @return the name of item
	 */
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
			ChatItem.debug("trp: " + trp);
			if (c.COLOR_IF_ALREADY_COLORED) {
				replacer = replacer.replace(NAME, ChatColor.stripColor(trp));
			} else {
				replacer = replacer.replace(NAME, ColorManager.fixColor(trp));
			}
		} else {
			replacer = replacer.replace(NAME, NamerManager.getName(p, item, c));
		}
		return replacer;
	}

	/**
	 * Get the name of item according to player & config
	 * 
	 * @param p the player
	 * @param item the item
	 * @param c the config
	 * @return the name of item or hand
	 */
	public static String getNameOfItem(Player p, ItemStack item, Storage c) {
		if(ItemUtils.isEmpty(item)) {
			if(c.HAND_DISABLED)
				return c.PLACEHOLDERS.get(0);
			else
				return c.HAND_NAME;
		}
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

	public static String calculateTime(long seconds) {
		Storage c = ChatItem.getInstance().getStorage();
		if (seconds < 60) {
			return seconds + c.SECONDS;
		}
		if (seconds < 3600) {
			StringBuilder builder = new StringBuilder();
			int minutes = (int) seconds / 60;
			builder.append(minutes).append(c.MINUTES);
			int secs = (int) seconds - minutes * 60;
			if (secs != 0) {
				builder.append(" ").append(secs).append(c.SECONDS);
			}
			return builder.toString();
		}
		StringBuilder builder = new StringBuilder();
		int hours = (int) seconds / 3600;
		builder.append(hours).append(c.HOURS);
		int minutes = (int) (seconds / 60) - (hours * 60);
		if (minutes != 0) {
			builder.append(" ").append(minutes).append(c.MINUTES);
		}
		int secs = (int) (seconds - ((seconds / 60) * 60));
		if (secs != 0) {
			builder.append(" ").append(secs).append(c.SECONDS);
		}
		return builder.toString();
	}

	public static boolean canShowItem(Player p, ItemStack item, @Nullable Cancellable e) {
		Storage c = ChatItem.getInstance().getStorage();
		if (c.PERMISSION_ENABLED && !p.hasPermission(c.PERMISSION_NAME)) {
			if (!c.LET_MESSAGE_THROUGH) {
				if(e != null)
					e.setCancelled(true);
			}
			if (!c.NO_PERMISSION_MESSAGE.isEmpty() && c.SHOW_NO_PERM_NORMAL) {
				sendIfNeed(p, c.NO_PERMISSION_MESSAGE);
			}
			return false;
		}
		if (item.getType().equals(Material.AIR)) {
			if (c.DENY_IF_NO_ITEM) {
				if(e != null)
					e.setCancelled(true);
				if (!c.DENY_MESSAGE.isEmpty())
					sendIfNeed(p, c.DENY_MESSAGE);
				return false;
			}
			if (c.HAND_DISABLED) {
				return false;
			}
		}
		if (c.COOLDOWN > 0 && !p.hasPermission("chatitem.ignore-cooldown")) {
			if (COOLDOWNS.containsKey(p.getUniqueId())) {
				long start = COOLDOWNS.get(p.getUniqueId());
				long current = System.currentTimeMillis() / 1000;
				long elapsed = current - start;
				if (elapsed >= c.COOLDOWN) {
					COOLDOWNS.remove(p.getUniqueId());
				} else {
					if (!c.LET_MESSAGE_THROUGH) {
						if(e != null)
							e.setCancelled(true);
					}
					if (!c.COOLDOWN_MESSAGE.isEmpty()) {
						long left = (start + c.COOLDOWN) - current;
						sendIfNeed(p, c.COOLDOWN_MESSAGE.replace(LEFT, ChatManager.calculateTime(left)));
					}
					ChatItem.debug("Cooldown");
					return false;
				}
			}
		}
		LAST_INFO_MESSAGE.put(p.getUniqueId(), System.currentTimeMillis()); // prevent showing item then send cooldown error message
		return true;
	}
	
	private static void sendIfNeed(Player p, String msg) {
		Long time = LAST_INFO_MESSAGE.remove(p.getUniqueId());
		if(time != null) {
			long diff = System.currentTimeMillis() - time;
			if(diff < 100)
				return; // don't show message
		}
		p.sendMessage(msg);
		LAST_INFO_MESSAGE.put(p.getUniqueId(), System.currentTimeMillis());
	}
	
	public static void clear(Player p) {
		COOLDOWNS.remove(p.getUniqueId());
		LAST_INFO_MESSAGE.remove(p.getUniqueId());
	}
	
	public static void applyCooldown(Player p) {
		COOLDOWNS.put(p.getUniqueId(), System.currentTimeMillis() / 1000);
	}
}
