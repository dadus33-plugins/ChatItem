package me.dadus33.chatitem.chatmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemSlot;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.hook.ecoenchants.EcoEnchantsSupport;
import me.dadus33.chatitem.invsee.InvShower;
import me.dadus33.chatitem.invsee.hook.EnderChestShower;
import me.dadus33.chatitem.invsee.hook.OneItemShower;
import me.dadus33.chatitem.invsee.hook.PlayerInventoryShower;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.utils.Colors;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.Messages;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;

@SuppressWarnings("deprecation")
public abstract class ChatManager {

	public static String inTest = null;
	private final static HashMap<UUID, Long> COOLDOWNS = new HashMap<>();
	private final static HashMap<UUID, Long> LAST_INFO_MESSAGE = new HashMap<>();
	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	private final static String LEFT = "{remaining}";
	public final static char SEPARATOR = ((char) 0x0007);
	public final static String SEPARATOR_STR = "\\u0007";
	public final static char SEPARATOR_END = ((char) 0x0003);
	public final static String SEPARATOR_END_STR = "\\u0003";

	public static String addSeparator(int id) {
		return SEPARATOR + Integer.toString(id) + SEPARATOR_END;
	}
	
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
	public static ItemStack getUsableItem(Player p, ItemSlot slot) {
		if (slot == null)
			return null;
		ItemStack betterItem = HandItem.getBetterItem(p, slot);
		if (betterItem == null)
			return null;

		ItemStack item = betterItem.clone();
		if (slot.isDenyIfNoItem() && ItemUtils.isEmpty(item))
			return null;
		if (EcoEnchantsSupport.hasSupport()) {
			item = EcoEnchantsSupport.manageItem(item);
		} else if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof BookMeta) { // filtering written books
				BookMeta bm = (BookMeta) item.getItemMeta();
				bm.setPages(Collections.emptyList());
				item.setItemMeta(bm);
			} else if (meta instanceof BlockStateMeta && Version.getVersion().isNewerOrEquals(Version.V1_9)) { // if it's a block
				BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
				if (bsm.hasBlockState() && bsm.getBlockState() instanceof ShulkerBox) {
					ShulkerBox sb = (ShulkerBox) bsm.getBlockState();
					for (ItemStack itemInv : sb.getInventory()) {
						ItemUtils.stripData(itemInv);
					}
					bsm.setBlockState(sb);
				}
				item.setItemMeta(bsm);
			}
		}
		return item;
	}

	public static ChatAction getChatAction(ItemSlot slot, Player p) {
		if (slot.isCommand()) {
			UUID uuid = UUID.randomUUID();
			InvShower.add(uuid.toString(), slot == ItemSlot.INVENTORY ? new PlayerInventoryShower(p) : new EnderChestShower(p));
			return new ChatAction(slot, p, "/chatitem seeinv " + uuid.toString());
		}
		UUID uuid = UUID.randomUUID();
		ItemStack item = getUsableItem(p, slot);
		InvShower.add(uuid.toString(), new OneItemShower(p, item));
		return new ChatAction(slot, p, item, "/chatitem seeinv " + uuid.toString());
	}

	/**
	 * Get all lores lines of an item, to replace the show_item action which is not
	 * working with ViaBackwards
	 * 
	 * @param item the item to get lines from
	 * @return all lores
	 */
	public static List<String> getMaxLinesFromItem(Player viewer, ItemStack item) {
		List<String> lines = new ArrayList<>();
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			lines.add(NamerManager.getName(viewer, item, ChatItem.getInstance().getStorage()));
			if (meta.hasEnchants()) {
				meta.getEnchants().forEach((enchant, lvl) -> {
					lines.add(Colors.RESET + Utils.getEnchantName(enchant) + " " + Utils.toRoman(lvl));
				});
			}
			if (meta.hasLore())
				lines.addAll(meta.getLore());
		} else {
			lines.add(NamerManager.getName(viewer, item, ChatItem.getInstance().getStorage()));
		}
		return lines;
	}

	/**
	 * Get the name of item according to player & config<br>
	 * Prefer use {@link #getNameOfItem(Player, ItemStack, Storage)} if you want
	 * take in count the empty item
	 * 
	 * @param viewer the player
	 * @param item   the item
	 * @param c      the config
	 * @return the   name of item
	 */
	public static String styleItem(Player viewer, ItemStack item, Storage c) {
		String replacer = c.nameFormat;
		String amount = c.amountFormat;
		if (item.getAmount() == 1) {
			if (c.addAmountForced) {
				amount = amount.replace(TIMES, "1");
				replacer = replacer.replace(AMOUNT, amount);
			} else {
				replacer = replacer.replace(AMOUNT, "");
			}
		} else {
			amount = amount.replace(TIMES, String.valueOf(item.getAmount()));
			replacer = replacer.replace(AMOUNT, amount);
		}
		return replacer.replace(NAME, NamerManager.getName(viewer, item, c));
	}

	/**
	 * Get the name of item according to player & config
	 * 
	 * @param p    the player that is owner of item
	 * @param item the item
	 * @param c    the config
	 * @return the name of item or hand
	 */
	public static String getNameOfItem(Player p, ItemStack item, Player viewer, Storage c) {
		if (ItemUtils.isEmpty(item)) {
			if (c.handDisabled)
				return ItemSlot.HAND.getPlaceholders().get(0);
			return getHandName(p);
		}
		return ChatItem.replace(p, styleItem(viewer, item, c));
	}

	public static String getNameForChatAction(Player viewer, Chat chat, Storage c) {
		ChatAction action = chat.getAction();
		if (action.hasItem()) {
			ItemStack item = action.getItem();
			if (ItemUtils.isEmpty(item)) {
				if (c.handDisabled)
					return chat.getSlot().getPlaceholders().get(0);
				return getHandName(chat);
			}
			return ChatItem.replace(chat.getPlayer(), styleItem(viewer, item, c));
		}
		return ChatItem.replace(chat.getPlayer(), Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", action.getOrigin().getName()));
	}

	public static String getNameForChatAction(Player viewer, ChatAction action, Storage c) {
		if (action.hasItem()) {
			ItemStack item = action.getItem();
			if (ItemUtils.isEmpty(item)) {
				if (c.handDisabled)
					return action.getSlot().getPlaceholders().get(0);
				else
					return getHandName(action.getOrigin());
			}
			return ChatItem.replace(action.getOrigin(), styleItem(viewer, item, c));
		}
		return ChatItem.replace(action.getOrigin(), Messages.getMessage(action.getSlot().name().toLowerCase() + ".chat", "%cible%", action.getOrigin().getName()));
	}

	@Deprecated
	public static String getHandName(Player p) {
		return ChatItem.replace(p, ChatItem.getInstance().getStorage().handName.replace("{name}", p.getName()).replace("{display-name}", p.getDisplayName()));
	}

	public static String getHandName(Chat c) {
		Player p = c.getPlayer();
		return ChatItem.replace(p, ChatItem.getInstance().getStorage().handName.replace("{name}", p.getName()).replace("{display-name}", p.getDisplayName()));
	}

	public static String calculateTime(long seconds) {
		Storage c = ChatItem.getInstance().getStorage();
		if (seconds < 60) {
			return seconds + c.SECONDS;
		}
		if (seconds < 3600) {
			StringBuilder builder = new StringBuilder();
			int minutes = (int) seconds / 60;
			builder.append(minutes).append(c.minutes);
			int secs = (int) seconds - minutes * 60;
			if (secs != 0) {
				builder.append(" ").append(secs).append(c.SECONDS);
			}
			return builder.toString();
		}
		StringBuilder builder = new StringBuilder();
		int hours = (int) seconds / 3600;
		builder.append(hours).append(c.hours);
		int minutes = (int) (seconds / 60) - (hours * 60);
		if (minutes != 0) {
			builder.append(" ").append(minutes).append(c.minutes);
		}
		int secs = (int) (seconds - ((seconds / 60) * 60));
		if (secs != 0) {
			builder.append(" ").append(secs).append(c.SECONDS);
		}
		return builder.toString();
	}

	public static boolean canUsePlaceholder(Player p, ChatAction action, ItemSlot slot, @Nullable Cancellable e) {
		Storage c = ChatItem.getInstance().getStorage();
		if (c.permissionEnabled && !p.hasPermission(c.permissionName)) {
			if (!c.letMessageThrough) {
				if (e != null)
					e.setCancelled(true);
			}
			if (!c.messageNoPermission.isEmpty() && c.showNoPermissionMessage) {
				sendIfNeed(p, c.messageNoPermission);
			}
			return false;
		}
		if (action.hasItem() && action.getItem().getType().equals(Material.AIR) && slot.isBasic()) {
			if (slot.isDenyIfNoItem()) {
				if (e != null)
					e.setCancelled(true);
				if (!c.messageDeny.isEmpty())
					sendIfNeed(p, c.messageDeny);
				return false;
			}
			if (c.handDisabled) {
				return false;
			}
		}
		if (c.cooldown > 0 && !p.hasPermission("chatitem.ignore-cooldown")) {
			if (COOLDOWNS.containsKey(p.getUniqueId())) {
				long start = COOLDOWNS.get(p.getUniqueId());
				long current = System.currentTimeMillis() / 1000;
				long elapsed = current - start;
				if (elapsed >= c.cooldown) {
					COOLDOWNS.remove(p.getUniqueId());
				} else {
					if (!c.letMessageThrough) {
						if (e != null)
							e.setCancelled(true);
					}
					if (!c.messageCooldown.isEmpty()) {
						long left = (start + c.cooldown) - current;
						sendIfNeed(p, c.messageCooldown.replace(LEFT, ChatManager.calculateTime(left)));
					}
					ChatItem.debug("Cooldown");
					return false;
				}
			}
		}
		if (action.hasItem()) {
			for (String ignored : c.ignoredItems) {
				if (action.getItem().getType().name().toLowerCase().contains(ignored.toLowerCase())) {
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

	public static boolean isTestingEnabled() {
		return inTest != null;
	}

	public static boolean isTesting(String actual) {
		return inTest != null && (inTest == "both" || inTest == "all" || inTest.equalsIgnoreCase(actual));
	}

	public static void setTesting(String actual) {
		inTest = actual;
	}

	public static boolean isSelected(String actual) {
		if(isTestingEnabled())
			return true;
		String selected = ChatItem.getInstance().getStorage().manager;
		if (selected.equalsIgnoreCase("both") || selected.equalsIgnoreCase("all"))
			return true;
		if (selected == "auto") {
			if (Utils.IS_PAPER && actual == "paper")
				return true;
			else if (actual == "packet" && ChatItem.getPluginThatRequirePacket().stream().map(Bukkit.getPluginManager()::getPlugin).anyMatch(Objects::nonNull))
				return true;
			else if (actual == "chat")
				return true;
		}
		return selected.equalsIgnoreCase(actual);
	}
}
