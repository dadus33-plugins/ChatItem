package me.dadus33.chatitem.chatmanager.v2;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.ColorManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Version;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

@SuppressWarnings("deprecation")
public class ChatListener implements Listener {

	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	private final static String LEFT = "{remaining}";
	private final HashMap<String, Long> COOLDOWNS = new HashMap<>();
	private ChatListenerChatManager manage;
	private Method saveMethod;

	public ChatListener(ChatListenerChatManager manage) {
		this.manage = manage;

		try {
			Class<?> nbtTag = PacketUtils.getNmsClass("NBTTagCompound", "nbt.");
			Class<?> itemClass = PacketUtils.getNmsClass("ItemStack", "world.item.");
			for (Method m : itemClass.getDeclaredMethods()) {
				if (m.getParameterTypes().length == 1) {
					if (m.getParameterTypes()[0].equals(nbtTag) && m.getReturnType().equals(nbtTag)) {
						saveMethod = m;
					}
				}
			}
		} catch (Exception e) {

		}
		if (saveMethod == null)
			ChatItem.getInstance().getLogger().info("Failed to find save method. Using default system...");
		else
			ChatItem.getInstance().getLogger().info("Save method founded: " + saveMethod.getName() + ".");
	}

	public Storage getStorage() {
		return manage.getStorage();
	}

	private String calculateTime(long seconds) {
		if (seconds < 60) {
			return seconds + getStorage().SECONDS;
		}
		if (seconds < 3600) {
			StringBuilder builder = new StringBuilder();
			int minutes = (int) seconds / 60;
			builder.append(minutes).append(getStorage().MINUTES);
			int secs = (int) seconds - minutes * 60;
			if (secs != 0) {
				builder.append(" ").append(secs).append(getStorage().SECONDS);
			}
			return builder.toString();
		}
		StringBuilder builder = new StringBuilder();
		int hours = (int) seconds / 3600;
		builder.append(hours).append(getStorage().HOURS);
		int minutes = (int) (seconds / 60) - (hours * 60);
		if (minutes != 0) {
			builder.append(" ").append(minutes).append(getStorage().MINUTES);
		}
		int secs = (int) (seconds - ((seconds / 60) * 60));
		if (secs != 0) {
			builder.append(" ").append(secs).append(getStorage().SECONDS);
		}
		return builder.toString();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent e) {
		if (e.isCancelled()) {
			if (ChatItem.getInstance().getChatManager().size() == 1) { // only chat
				String msg = e.getMessage().toLowerCase();
				for (String rep : getStorage().PLACEHOLDERS) {
					if (msg.contains(rep)) {
						ChatItem.debug(
								"You choose 'chat' manager but it seems to don't be the good choice. More informations here: https://github.com/dadus33-plugins/ChatItem/wiki");
						return;
					}
				}
			}
			ChatItem.debug("Chat cancelled for " + e.getPlayer().getName());
			return;
		}
		Player p = e.getPlayer();
		boolean found = false;

		for (String rep : getStorage().PLACEHOLDERS) {
			if (e.getMessage().contains(rep + ChatManager.SEPARATOR + p.getName())) // already managed by v1
				return;
			if (e.getMessage().contains(rep)) {
				found = true;
				break;
			}
		}

		if (!found) {
			return;
		}
		if (getStorage().PERMISSION_ENABLED && !p.hasPermission(getStorage().PERMISSION_NAME)) {
			if (!getStorage().LET_MESSAGE_THROUGH) {
				e.setCancelled(true);
			}
			if (!getStorage().NO_PERMISSION_MESSAGE.isEmpty() && getStorage().SHOW_NO_PERM_NORMAL) {
				p.sendMessage(getStorage().NO_PERMISSION_MESSAGE);
			}
			return;
		}
		if (p.getItemInHand().getType().equals(Material.AIR)) {
			if (getStorage().DENY_IF_NO_ITEM) {
				e.setCancelled(true);
				if (!getStorage().DENY_MESSAGE.isEmpty())
					e.getPlayer().sendMessage(getStorage().DENY_MESSAGE);
				return;
			}
			if (getStorage().HAND_DISABLED) {
				return;
			}
		}
		if (getStorage().COOLDOWN > 0 && !p.hasPermission("chatitem.ignore-cooldown")) { // use cooldown
			if (COOLDOWNS.containsKey(p.getName())) {
				long start = COOLDOWNS.get(p.getName());
				long current = System.currentTimeMillis() / 1000;
				long elapsed = current - start;
				if (elapsed >= getStorage().COOLDOWN) {
					COOLDOWNS.remove(p.getName());
				} else {
					if (!getStorage().LET_MESSAGE_THROUGH) {
						e.setCancelled(true);
					}
					if (!getStorage().COOLDOWN_MESSAGE.isEmpty()) {
						long left = (start + getStorage().COOLDOWN) - current;
						p.sendMessage(getStorage().COOLDOWN_MESSAGE.replace(LEFT, calculateTime(left)));
					}
					return;
				}
			}
			COOLDOWNS.put(p.getName(), System.currentTimeMillis() / 1000);
		}
		e.setCancelled(true);
		String format = e.getFormat();
		boolean isAlreadyParsed = false;
		if (format.contains("%1$s") && format.contains("%2$s")) // message not parsed but not default way
			isAlreadyParsed = false;
		if (format.equalsIgnoreCase(e.getMessage())) // is message already parsed
			isAlreadyParsed = true;
		if (format.equalsIgnoreCase("<%1$s> %2$s")) // default MC message
			isAlreadyParsed = false;
		String msg;
		if(isAlreadyParsed) {
			String defMsg = e.getMessage();
			ChatItem.debug("Begin def msg: " + defMsg + "");
			for (String rep : getStorage().PLACEHOLDERS) {
				defMsg = defMsg.replace(rep, ChatManager.SEPARATOR_STR);
			}
			msg = String.format(format, p.getDisplayName(), defMsg);
		} else {
			String defMsg = e.getMessage();
			for (String rep : getStorage().PLACEHOLDERS) {
				defMsg = defMsg.replace(rep, ChatManager.SEPARATOR + "");
			}
			msg = format.replace(e.getMessage(), defMsg);
		}
		ChatItem.debug("msg: " + msg + ", format: " + format);
		ItemStack item = p.getItemInHand();
		if(Version.getVersion().isNewerOrEquals(Version.V1_16))
			e.getRecipients().forEach((pl) -> showWithHex(pl, p, item, msg));
		else
			e.getRecipients().forEach((pl) -> showWithoutHex(pl, p, item, msg));
		Bukkit.getConsoleSender().sendMessage(msg); // show in log
	}
	
	private void showWithoutHex(Player to, Player origin, ItemStack item, String msg) {
		ComponentBuilder builder = new ComponentBuilder("");
		ChatColor color = ChatColor.WHITE;
		String colorCode = "", text = "";
		boolean waiting = false;
		for(char args : msg.toCharArray()) {
			if(args == ChatManager.SEPARATOR && (!getStorage().HAND_DISABLED || (item != null && item.hasItemMeta()))) {
				builder.append(text);
				// here put the item
				addItem(builder, to, origin, item);
			} else  if(args == '§') { // begin of color
				if(colorCode.isEmpty() && !text.isEmpty()) { // text before this char
					ChatItem.debug("Append " + text);
					builder.append(new ComponentBuilder(text).color(color).create());
					text = "";
				}
				
				waiting = true; // waiting for color code
			} else if(waiting) { // if waiting for code and valid str
				if(String.valueOf(args).matches("-?[0-9a-fA-F]+") && colorCode.length() <= 5) { // if it's hexademical value and with enough space for full color
					colorCode += args; // add char to it
					waiting = false;
				} else {
					color = ChatColor.getByChar(args); // a color by itself
					colorCode = ""; // clean actual code, it's only to prevent some kind of issue
					waiting = false;
				}
			} else {
				if(!colorCode.isEmpty()) {
					color = ColorManager.getColor(colorCode);
					colorCode = ""; // clean actual code
					builder.append(color.toString());
				}
				// basic text, not waiting for code after '§'
				text += args;
				ChatItem.debug("arg: " + args);
				waiting = false;
			}
		}
		builder.append(text);
		to.spigot().sendMessage(builder.create());
	}

	private void showWithHex(Player to, Player origin, ItemStack item, String msg) {
		ComponentBuilder builder = new ComponentBuilder("");
		ChatColor color = ChatColor.WHITE;
		String colorCode = "", text = "";
		boolean waiting = false;
		for(char args : msg.toCharArray()) {
			if(args == ChatManager.SEPARATOR && (!getStorage().HAND_DISABLED || (item != null && item.hasItemMeta()))) {
				builder.append(text);
				// here put the item
				addItem(builder, to, origin, item);
			} else  if(args == '§') { // begin of color
				if(colorCode.isEmpty() && !text.isEmpty()) { // text before this char
					ChatItem.debug("Append " + text);
					builder.append(new ComponentBuilder(text).color(color).create());
					text = "";
				}
				
				waiting = true; // waiting for color code
			} else if(waiting) { // if waiting for code and valid str
				if(String.valueOf(args).matches("-?[0-9a-fA-F]+") && colorCode.length() <= 5) { // if it's hexademical value and with enough space for full color
					colorCode += args; // add char to it
					waiting = false;
				} else {
					color = ChatColor.getByChar(args); // a color by itself
					colorCode = ""; // clean actual code, it's only to prevent some kind of issue
					waiting = false;
				}
			} else {
				if(!colorCode.isEmpty()) {
					color = ColorManager.getColor(colorCode);
					colorCode = ""; // clean actual code
					builder.append(color.toString());
				}
				// basic text, not waiting for code after '§'
				text += args;
				ChatItem.debug("arg: " + args);
				waiting = false;
			}
		}
		builder.append(text);
		to.spigot().sendMessage(builder.create());
	}

	/**
	 * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string for
	 * sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
	 *
	 * @param itemStack the item to convert
	 * @return the Json string representation of the item
	 */
	public String convertItemStackToJson(ItemStack itemStack) {
		try {
			Class<?> nbtTag = PacketUtils.getNmsClass("NBTTagCompound", "nbt.");
			Class<?> craftItemClass = PacketUtils.getObcClass("inventory.CraftItemStack");
			Object nmsNbtTagCompoundObj = nbtTag.newInstance();
			if (saveMethod == null) {
				Object nmsItemStackObj = craftItemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
				return nmsItemStackObj.getClass().getMethod("save", nbtTag)
						.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
			} else {
				Object nmsItemStackObj = craftItemClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
				return saveMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void addItem(ComponentBuilder builder, Player to, Player origin, ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ComponentBuilder itemComponent = new ComponentBuilder(
					ChatListener.styleItem(to, item, getStorage()));
			String itemJson = convertItemStackToJson(item);
			itemComponent.event(new HoverEvent(Action.SHOW_ITEM, new ComponentBuilder(itemJson).create()));
			builder.append(itemComponent.create());
		} else {
			String handName = getStorage().HAND_NAME;
			ComponentBuilder handComp = new ComponentBuilder("");
			ComponentBuilder handTooltip = new ComponentBuilder("");
			int stay = getStorage().HAND_TOOLTIP.size();
			for (String line : getStorage().HAND_TOOLTIP) {
				stay--;
				handTooltip.append(line.replace("{name}", origin.getName()).replace("{display-name}",
						origin.getDisplayName()));
				if (stay > 0)
					handTooltip.append("\n");
			}
			handComp.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, handTooltip.create()));
			if (handName.contains("{name}")) {
				String[] splitted = handName.split("{name}");
				for (int i = 0; i < (splitted.length - 1); i++) {
					handComp.append(new ComponentBuilder(splitted[i]).create());
					handComp.append(PlayerNamerManager.getPlayerNamer().getName(origin));
				}
				handComp.append(new ComponentBuilder(splitted[splitted.length - 1]).create());
			} else
				handComp.append(handName.replace("{display-name}", origin.getDisplayName()));
			builder.append(handComp.create());
		}
	}

	public static String styleItem(Player p, ItemStack item, Storage c) {
		String replacer = c.NAME_FORMAT;
		String amount = c.AMOUNT_FORMAT;

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
		replacer = replacer.replace(NAME, NamerManager.getName(p, item, c));
		return replacer;
	}
}
