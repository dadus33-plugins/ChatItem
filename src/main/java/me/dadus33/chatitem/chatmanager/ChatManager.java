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
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;

public abstract class ChatManager {

	private final static HashMap<UUID, Long> COOLDOWNS = new HashMap<>();
	private final static HashMap<UUID, Long> LAST_INFO_MESSAGE = new HashMap<>();
	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	private final static String LEFT = "{remaining}";
	public final static char SEPARATOR = ((char) 0x0007);
	public final static String SEPARATOR_STR = "\\u0007";
	public final static char SEPARATOR_END = ((char) 0x0008);
	public final static String SEPARATOR_END_STR = "\\u0008";

	public static String removeSeparator(String message) {
		return fixSeparator(message).replace(Character.toString(SEPARATOR), "").replace(Character.toString(SEPARATOR_END), "");
	}

	public static String replaceSeparator(Chat chat, String message, String replacement) {
		return fixSeparator(message).replace(Character.toString(SEPARATOR) + (chat != null ? chat.getId() : "") + Character.toString(SEPARATOR_END), replacement);
	}

	public static String fixSeparator(String s) {
		return s == null ? "" : s.replace(SEPARATOR_STR, Character.toString(SEPARATOR)).replace(SEPARATOR_END_STR, Character.toString(SEPARATOR_END));
	}

	public static boolean equalsSeparator(String s) {
		return s != null && (s.equalsIgnoreCase(SEPARATOR_STR) || s.equalsIgnoreCase(Character.toString(SEPARATOR)));
	}

	public static boolean containsSeparator(String s) {
		return s != null && (s.contains(SEPARATOR_STR) || s.contains(Character.toString(SEPARATOR)));
	}

	public static boolean equalsSeparatorEnd(String s) {
		return s != null && (s.equalsIgnoreCase(SEPARATOR_END_STR) || s.equalsIgnoreCase(Character.toString(SEPARATOR_END)));
	}

	public static boolean containsSeparatorEnd(String s) {
		return s != null && (s.contains(SEPARATOR_END_STR) || s.contains(Character.toString(SEPARATOR_END)));
	}

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
		/*
		 * if(ChatItem.ecoEnchantsSupport) { List<String> addLore =
		 * EcoEnchantsSupport.getLores(item); if(!addLore.isEmpty()) { ItemMeta meta =
		 * item.getItemMeta(); List<String> lores = meta.hasLore() ? meta.getLore() :
		 * new ArrayList<>(); for(int i = 0; i < addLore.size(); i++) lores.add(i,
		 * addLore.get(i));
		 * 
		 * meta.setLore(lores); item.setItemMeta(meta); ChatItem.debug("Added " +
		 * addLore.size() + " lores from EcoEnchants"); } else
		 * ChatItem.debug("No lore to add from EcoEnchants"); }
		 */
		return item;
	}

	/**
	 * Get all lores lines of an item, to replace the show_item action which is not
	 * working with ViaBackwards
	 * 
	 * @param item the item to get lines from
	 * @return all lores
	 */
	public static List<String> getMaxLinesFromItem(Player p, ItemStack item) {
		List<String> lines = new ArrayList<>();
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			lines.add(meta.hasDisplayName() ? meta.getDisplayName() : NamerManager.getName(p, item, ChatItem.getInstance().getStorage()));
			if (meta.hasEnchants()) {
				meta.getEnchants().forEach((enchant, lvl) -> {
					lines.add(ChatColor.RESET + Utils.getEnchantName(enchant) + " " + Utils.toRoman(lvl));
				});
			}
			if (meta.hasLore())
				lines.addAll(meta.getLore());
		} else {
			lines.add(NamerManager.getName(p, item, ChatItem.getInstance().getStorage()));
		}
		return lines;
	}

	/**
	 * Get the name of item according to player & config<br>
	 * Prefer use {@link #getNameOfItem(Player, ItemStack, Storage)} if you want
	 * take in count the empty item
	 * 
	 * @param p    the player
	 * @param item the item
	 * @param c    the config
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
				replacer = replacer.replace(NAME, trp);
			}
		} else {
			replacer = replacer.replace(NAME, NamerManager.getName(p, item, c));
		}
		return replacer;
	}

	/**
	 * Get the name of item according to player & config
	 * 
	 * @param p    the player that is owner of item
	 * @param item the item
	 * @param c    the config
	 * @return the name of item or hand
	 */
	public static String getNameOfItem(Player p, ItemStack item, Storage c) {
		if (ItemUtils.isEmpty(item)) {
			if (c.HAND_DISABLED)
				return c.PLACEHOLDERS.get(0);
			else
				return c.HAND_NAME;
		}
		return styleItem(p, item, c);
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
				if (e != null)
					e.setCancelled(true);
			}
			if (!c.NO_PERMISSION_MESSAGE.isEmpty() && c.SHOW_NO_PERM_NORMAL) {
				sendIfNeed(p, c.NO_PERMISSION_MESSAGE);
			}
			return false;
		}
		if (item.getType().equals(Material.AIR)) {
			if (c.DENY_IF_NO_ITEM) {
				if (e != null)
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
						if (e != null)
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
		if (time != null) {
			long diff = System.currentTimeMillis() - time;
			if (diff < 100)
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
