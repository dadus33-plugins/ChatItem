package me.dadus33.chatitem.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
import me.dadus33.chatitem.packets.ChatItemPacket;
import me.dadus33.chatitem.packets.PacketContent;
import me.dadus33.chatitem.packets.PacketHandler;
import me.dadus33.chatitem.packets.PacketMetadata;
import me.dadus33.chatitem.packets.PacketType;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ProtocolVersion;
import me.dadus33.chatitem.utils.Storage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketListenerV2 extends PacketHandler {

	private final static String NAME = "{name}";
	private final static String AMOUNT = "{amount}";
	private final static String TIMES = "{times}";
	private final static List<Material> SHULKER_BOXES = new ArrayList<>();

	private Storage c;

	public ChatPacketListenerV2(Storage s) {
		if (ProtocolVersion.getServerVersion().isNewerOrEquals(ProtocolVersion.V1_11)) {
			SHULKER_BOXES.addAll(Arrays.asList(Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
					Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
					Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX,
					Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
					Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
					Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX));
		}
		c = s;
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

	@SuppressWarnings("deprecation")
	@Override
	public void onSend(ChatItemPacket e) {
    	if(!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT))
    		return;
		final PacketContent packet = e.getContent();
		final PacketMetadata meta = e.getMetaData();
		if (!meta.hasMeta("parse")) { // First we check if the packet validator has validated this packet to be parsed
										// by us
			return;
		}
		boolean usesBaseComponents = meta.getMeta("base-component", false); // The packet validator should also tell if this
																							// packet uses base
																							// components
		e.setCancelled(true); // We cancel the packet as we're going to resend it anyways (ignoring listeners
								// this time)
		Bukkit.getScheduler().runTaskAsynchronously(ChatItem.getInstance(), () -> {
			String json = (String) meta.getMeta("json", ""); // The packet validator got the json for us, so no need to
																// get it again
			int topIndex = -1;
			String name = null;
			for (Player p : Bukkit.getOnlinePlayers()) {
				String pname = "\\u0007" + p.getName();
				if (!json.contains(pname)) {
					continue;
				}
				int index = json.lastIndexOf(pname) + pname.length();
				if (index > topIndex) {
					topIndex = index;
					name = pname.replace("\\u0007", "");
				}
			}
			if (name == null) { // something went really bad, so we run away and hide (AKA the player left or is
								// on another server)
				return;
			}

			Player p = Bukkit.getPlayer(name);
			StringBuilder builder = new StringBuilder(json);
			builder.replace(topIndex - (name.length() + 6), topIndex, ""); // we remove both the name and the separator
																			// from the json string
			json = builder.toString();

			String message = null;
			try {
				if (!p.getItemInHand().getType().equals(Material.AIR)) {
					ItemStack copy = p.getItemInHand().clone();
					if (copy.getType().name().contains("_BOOK")) { // filtering written books
						BookMeta bm = (BookMeta) copy.getItemMeta();
						bm.setPages(Collections.emptyList());
						copy.setItemMeta(bm);
					} else {
						if (ProtocolVersion.getServerVersion().isNewerOrEquals(ProtocolVersion.V1_11)) { // filtering
																											// shulker
																											// boxes
							if (SHULKER_BOXES.contains(copy.getType())) {
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
					}
					message = ChatItem.getManipulator().parse(json, c.PLACEHOLDERS, copy, styleItem(copy, c),
							ProtocolVersion.getClientVersion(e.getPlayer()));
				} else {
					if (!c.HAND_DISABLED) {
						message = ChatItem.getManipulator().parseEmpty(json, c.PLACEHOLDERS, c.HAND_NAME,
								c.HAND_TOOLTIP, p);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (message != null) {
				if (!usesBaseComponents) {
					packet.getChatComponents().write(0, jsonToChatComponent(message));
				} else {
					packet.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
				}
			}
			PacketUtils.sendPacket(p, e.getPacket());
		});
	}
	
	private Object jsonToChatComponent(String json) {
		try {
			return PacketUtils.CHAT_SERIALIZER.getMethod("a", String.class).invoke(null, json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setStorage(Storage st) {
		c = st;
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

}
