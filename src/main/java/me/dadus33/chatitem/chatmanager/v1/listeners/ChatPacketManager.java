package me.dadus33.chatitem.chatmanager.v1.listeners;

import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR;
import static me.dadus33.chatitem.chatmanager.ChatManager.SEPARATOR_STR;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.AdventureComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.BaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.ComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.IChatBaseComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.StringComponentGetter;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketHandler;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketManager extends PacketHandler {

	private Object lastSentPacket = null;
	private Method serializerGetJson;
	private PacketEditingChatManager manager;
	private final List<IBaseComponentGetter> baseComponentGetter = new ArrayList<>();

	public ChatPacketManager(PacketEditingChatManager manager) {
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
		for(IBaseComponentGetter getter : Arrays.asList(new IChatBaseComponentGetter(), new BaseComponentGetter(), new ComponentGetter(), new AdventureComponentGetter())) {
			if(getter.hasConditions())
				baseComponentGetter.add(getter);
		}
		ChatItem.getInstance().getLogger().info("Loaded " + baseComponentGetter.size() + " getter for base components.");
	}

	@Override
	public void onSend(ChatItemPacket e) {
		if (!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT))
			return;
		if (lastSentPacket != null && lastSentPacket == e.getPacket())
			return; // prevent infinite loop
		//if(e.getPacket().getClass().getName().equalsIgnoreCase("ClientboundSystemChatPacket")) {
			for(Field f : e.getPacket().getClass().getDeclaredFields()) {
				try {
					f.setAccessible(true);
					ChatItem.debug(" " + f.getName() +": " + f.get(e.getPacket()));
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		//}
		PacketContent packet = e.getContent();
		Version version = Version.getVersion();
		String json = "{}";
		IBaseComponentGetter choosedGetter = null;
		if (version.isNewerOrEquals(Version.V1_19)) {
			if(packet.getIntegers().readSafely(0) != 1)
				return;
			String foundedJson = packet.getStrings().read(0); // for final things
			json = foundedJson;
			choosedGetter = new StringComponentGetter();
		} else if (version.isNewerOrEquals(Version.V1_12)) {
			// only if action bar messages are supported in this version of minecraft
			try {
				if (((Enum<?>) packet
						.getSpecificModifier(PacketUtils.getNmsClass("ChatMessageType", "network.chat.")).read(0))
								.name().equals("GAME_INFO")) {
					return; // It means it's an actionbar message, and we ain't intercepting those
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		} else if (version.isNewerOrEquals(Version.V1_8) && packet.getBytes().readSafely(0) == (byte) 2) {
			return; // It means it's an actionbar message, and we ain't intercepting those
		}
		if(json == null || choosedGetter == null) {
			for(IBaseComponentGetter getters : baseComponentGetter) {
				String tmpJson = getters.getBaseComponentAsJSON(e);
				if(tmpJson != null) {
					ChatItem.debug("Found " + tmpJson + " with " + getters.getClass().getName());
					json = tmpJson;
					choosedGetter = getters;
					break;
				}
			}
		}
		boolean found = false;
		for (String rep : getStorage().PLACEHOLDERS) {
			if (json.contains(rep)) {
				found = true;
				break;
			}
		}
		if (!found) {
			ChatItem.debug("No placeholders founded in " + json);
			return; // then it's just a normal message without placeholders, so we leave it alone
		}
		Object toReplace = null;
		if (json.lastIndexOf(SEPARATOR) != -1)
			toReplace = SEPARATOR;
		if (json.lastIndexOf(SEPARATOR_STR) != -1)
			toReplace = SEPARATOR_STR;
		if (toReplace == null) { // if the message doesn't contain the BELL separator
			ChatItem.debug("Not containing bell " + json);
			return;
		}
		ChatItem.debug("Add packet meta to json: " + json);
		IBaseComponentGetter fchoosedGetter = choosedGetter;
		String fjson = json, toReplaceStr = toReplace.toString();
		e.setCancelled(true); // We cancel the packet as we're going to resend it anyways (ignoring listeners
		// this time)
		Bukkit.getScheduler().runTaskAsynchronously(ChatItem.getInstance(), () -> {
			Player p = e.getPlayer();
			int topIndex = -1;
			String name = null;
			for (Player pl : Bukkit.getOnlinePlayers()) {
				String pname = toReplaceStr + pl.getName();
				if (!fjson.contains(pname)) {
					continue;
				}
				int index = fjson.lastIndexOf(pname) + pname.length();
				if (index > topIndex) {
					topIndex = index;
					name = pname.replace(toReplaceStr, "");
				}
			}
			if (name == null) { // something went really bad, so we run away and hide (AKA the player left or is
				// on another server)
				ChatItem.debug("Name null for " + fjson);
				return;
			}

			Player itemPlayer = Bukkit.getPlayer(name);
			StringBuilder builder = new StringBuilder(fjson);
			builder.replace(topIndex - (name.length() + 6), topIndex, ""); // we remove both the name and the separator
			// from the json string
			String localJson = fjson.replace(toReplaceStr + itemPlayer.getName(), "");// builder.toString();

			String message = null;
			try {
				ItemStack item = ChatManager.getUsableItem(itemPlayer);
				if (!ItemUtils.isEmpty(item)) {
					ItemStack copy = item.clone();

					if (ItemPlayer.getPlayer(p).isBuggedClient()) {
						String act = getStorage().BUGGED_CLIENT_ACTION;
						List<String> tooltip;// = act.equalsIgnoreCase("nothing") ? new ArrayList<>() : null;
						if (act.equalsIgnoreCase("tooltip"))
							tooltip = getStorage().BUGGED_CLIENTS_TOOLTIP;
						else if (act.equalsIgnoreCase("item"))
							tooltip = getMaxLinesFromItem(p, copy);
						else if (act.equalsIgnoreCase("show_both")) {
							tooltip = getMaxLinesFromItem(p, copy);
							tooltip.addAll(getStorage().BUGGED_CLIENTS_TOOLTIP);
						} else
							tooltip = new ArrayList<>();
						message = manager.getManipulator().parseEmpty(localJson, getStorage().PLACEHOLDERS.get(0),
								ChatManager.styleItem(p, copy, getStorage()), tooltip, itemPlayer);
						if (!manager.supportsChatComponentApi()) {
							ChatItem.debug("Use basic for 1.7 lunar");
							packet.getChatComponents().write(0, jsonToChatComponent(message));
						} else {
							ChatItem.debug("Use baseComponent for 1.7 lunar");
							packet.getSpecificModifier(BaseComponent[].class).write(0,
									ComponentSerializer.parse(message));
						}
						lastSentPacket = e.getPacket();
						PacketUtils.sendPacket(p, lastSentPacket);
						return;
					}
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
									for (ItemStack itemInv : sb.getInventory()) {
										stripData(itemInv);
									}
									bsm.setBlockState(sb);
								}
								copy.setItemMeta(bsm);
							}
						}
					}
					message = manager.getManipulator().parse(localJson, getStorage().PLACEHOLDERS.get(0), copy,
							ChatManager.styleItem(p, copy, getStorage()), ItemPlayer.getPlayer(itemPlayer).getProtocolVersion());
				} else {
					if (!getStorage().HAND_DISABLED) {
						message = manager.getManipulator().parseEmpty(localJson, getStorage().PLACEHOLDERS.get(0),
								getStorage().HAND_NAME, getStorage().HAND_TOOLTIP, itemPlayer);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (message != null) {
				ChatItem.debug("(v1) Writing message: " + message);
				fchoosedGetter.writeJson(e, message);
			}
			lastSentPacket = e.getPacket();
			PacketUtils.sendPacket(p, lastSentPacket);
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

	private List<String> getMaxLinesFromItem(Player p, ItemStack item) {
		List<String> lines = new ArrayList<>();
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			lines.add(meta.hasDisplayName() ? meta.getDisplayName()
					: NamerManager.getName(p, item, getStorage()));
			if (meta.hasEnchants()) {
				meta.getEnchants().forEach((enchant, lvl) -> {
					lines.add(ChatColor.RESET + Utils.getEnchantName(enchant) + " " + Utils.toRoman(lvl));
				});
			}
			if (meta.hasLore())
				lines.addAll(meta.getLore());
		} else {
			lines.add(NamerManager.getName(p, item, getStorage()));
		}
		return lines;
	}
}
