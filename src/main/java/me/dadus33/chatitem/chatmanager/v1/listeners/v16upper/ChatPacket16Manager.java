package me.dadus33.chatitem.chatmanager.v1.listeners.v16upper;

import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketHandler;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacket16Manager extends PacketHandler {

	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	
	private Method serializerGetJson;
	private PacketEditingChatManager manager;

	public ChatPacket16Manager(PacketEditingChatManager manager) {
		this.manager = manager;
		try {
			for (Method m : PacketUtils.CHAT_SERIALIZER.getDeclaredMethods()) {
				if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS)
						&& m.getReturnType().equals(String.class)) {
					serializerGetJson = m;
					break;
				}
			}
			if (serializerGetJson == null)
				ChatItem.getInstance().getLogger().warning(
						"Failed to find JSON serializer in class: " + PacketUtils.CHAT_SERIALIZER.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSend(ChatItemPacket e) {
		if (!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT)) {
			return;
		}
		UUID sender = e.getContent().getSpecificModifier(UUID.class).readSafely(0, null);
		if(sender.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) // not chat message
			return;
		ChatItem.debug("UUID: " + sender);
		PacketContent packet = e.getContent();
		BaseComponent[] components = packet.getSpecificModifier(BaseComponent[].class).readSafely(0);
		if (components == null) {
			ChatItem.debug("No base components");
			return;
		}
		String json = ComponentSerializer.toString(components);

		boolean found = false;
		for (String rep : getStorage().PLACEHOLDERS) {
			if (json.contains(rep)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ChatItem.debug("No placeholders founded");
			return; // then it's just a normal message without placeholders, so we leave it alone
		}
		if (json.lastIndexOf(SEPARATOR) == -1) { // if the message doesn't contain the BELL separator, then it's
													// certainly NOT a message we want to parse
			ChatItem.debug("Not contains bell " + json);
			return;
		}
		ChatItem.debug("Add packet meta");
		e.setCancelled(true); // We cancel the packet as we're going to resend it anyways (ignoring listeners
		// this time)
		Bukkit.getScheduler().runTaskAsynchronously(ChatItem.getInstance(), () -> {

			Player p = Bukkit.getPlayer(sender);
			StringBuilder builder = new StringBuilder(json);
			// from the json string
			String localJson = builder.toString();

			String message = null;
			try {
				if (!p.getItemInHand().getType().equals(Material.AIR)) {
					ItemStack copy = p.getItemInHand().clone();
					if (copy.getType().name().contains("_BOOK")) { // filtering written books
						BookMeta bm = (BookMeta) copy.getItemMeta();
						bm.setPages(Collections.emptyList());
						copy.setItemMeta(bm);
					} else {
						if (copy.getType().name().contains("SHULKER_BOX")) { // if it's shulker
							if (copy.hasItemMeta()) {
								BlockStateMeta bsm = (BlockStateMeta) copy.getItemMeta();
								if (bsm.hasBlockState()) {
									ShulkerBox sb = (ShulkerBox) bsm.getBlockState();
									for (ItemStack item : sb.getInventory()) {
										stripData(item);
									}
									bsm.setBlockState(sb);
								}
								copy.setItemMeta(bsm);
							}
						}
					}
					message = manager.getManipulator().parse(localJson, getStorage().PLACEHOLDERS, copy,
							styleItem(copy, getStorage()), manager.getPlayerVersionAdapter().getProtocolVersion(p));
				} else {
					if (!getStorage().HAND_DISABLED) {
						message = manager.getManipulator().parseEmpty(localJson, getStorage().PLACEHOLDERS,
								getStorage().HAND_NAME, getStorage().HAND_TOOLTIP, p);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (message != null) {
				packet.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
			}
			PacketUtils.sendPacket(e.getPlayer(), e.getPacket());
		});
	}

	public static String styleItem(ItemStack item, Storage c) {
		String replacer = c.NAME_FORMAT;
		String amount = c.AMOUNT_FORMAT;
		boolean dname = item.hasItemMeta() ? item.getItemMeta().hasDisplayName() : false;

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
			HashMap<Short, String> translationSection = c.TRANSLATIONS.get(item.getType().name());
			if (translationSection == null) {
				String trp = materialToName(item.getType());
				replacer = replacer.replace(NAME, trp);
			} else {
				@SuppressWarnings("deprecation")
				String translated = translationSection.get(item.getDurability());
				if (translated != null) {
					replacer = replacer.replace(NAME, translated);
				} else {
					replacer = replacer.replace(NAME, materialToName(item.getType()));
				}
			}
		}
		return replacer;
	}

	private static String materialToName(Material m) {
		if (m.equals(Material.TNT)) {
			return "TNT";
		}
		String orig = m.toString().toLowerCase();
		String[] splits = orig.split("_");
		StringBuilder sb = new StringBuilder(orig.length());
		int pos = 0;
		for (String split : splits) {
			sb.append(split);
			int loc = sb.lastIndexOf(split);
			char charLoc = sb.charAt(loc);
			if (!(split.equalsIgnoreCase("of") || split.equalsIgnoreCase("and") || split.equalsIgnoreCase("with")
					|| split.equalsIgnoreCase("on")))
				sb.setCharAt(loc, Character.toUpperCase(charLoc));
			if (pos != splits.length - 1)
				sb.append(' ');
			++pos;
		}

		return sb.toString();
	}

	private void stripData(ItemStack i) {
		if (i == null) {
			return;
		}
		if (i.getType().equals(Material.AIR)) {
			return;
		}
		if (!i.hasItemMeta()) {
			return;
		}
		ItemMeta im = Bukkit.getItemFactory().getItemMeta(i.getType());
		ItemMeta original = i.getItemMeta();
		if (original.hasDisplayName()) {
			im.setDisplayName(original.getDisplayName());
		}
		i.setItemMeta(im);
	}


	public Storage getStorage() {
		return manager.getStorage();
	}
}
