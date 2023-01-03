package me.dadus33.chatitem.chatmanager.v1.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.ItemPlayer;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.AdventureComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.ComponentNMSManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.IChatBaseComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.StringComponentManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketHandler;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatPacketManager extends PacketHandler {

	private Object lastSentPacket = null;
	private Method serializerGetJson;
	private PacketEditingChatManager manager;
	private final List<IComponentManager> componentManager = new ArrayList<>();

	public ChatPacketManager(PacketEditingChatManager manager) {
		this.manager = manager;
		try {
			for (Method m : PacketUtils.CHAT_SERIALIZER.getDeclaredMethods()) {
				if (m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(PacketUtils.COMPONENT_CLASS) && m.getReturnType().equals(String.class)) {
					serializerGetJson = m;
					break;
				}
			}
			if (serializerGetJson == null)
				ChatItem.getInstance().getLogger().warning("Failed to find JSON serializer in class: " + PacketUtils.CHAT_SERIALIZER.getCanonicalName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (IComponentManager getter : Arrays.asList(new IChatBaseComponentManager(), new ComponentNMSManager(), new StringComponentManager())) {
			tryRegister(getter);
		}
		try {
			if (Class.forName("net.kyori.adventure.text.Component") != null)
				tryRegister(new AdventureComponentManager());
		} catch (Exception e) {
		}
		ChatItem.getInstance().getLogger().info("Loaded " + componentManager.size() + " getter for base components.");
		ChatItem.debug("ComponentManager: " + String.join(", ", componentManager.stream().map(IComponentManager::getClass).map(Class::getSimpleName).collect(Collectors.toList())));
	}

	private void tryRegister(IComponentManager getter) {
		if (getter.hasConditions())
			componentManager.add(getter);
	}

	@Override
	public void onSend(ChatItemPacket e) {
		if (!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT))
			return;
		if (lastSentPacket != null && lastSentPacket == e.getPacket())
			return; // prevent infinite loop
		ChatItem.debug("Checking: " + e.getPacketType().getFullName() + " to " + e.getPlayername());
		PacketContent packet = e.getContent();
		Version version = Version.getVersion();
		String json = "{}";
		IComponentManager choosedGetter = null;
		if (version.isNewerOrEquals(Version.V1_19)) {
			if (packet.getIntegers().readSafely(0, 0) > 1) { // not parsed chat message type
				ChatItem.debug("Invalid int: " + packet.getIntegers().read(0));
				return;
			}
			choosedGetter = new StringComponentManager();
			json = choosedGetter.getBaseComponentAsJSON(e); // if null, will be re-checked so anyway
		} else if (version.isNewerOrEquals(Version.V1_12)) {
			// only if action bar messages are supported
			if (((Enum<?>) packet.getSpecificModifier(PacketUtils.getNmsClass("ChatMessageType", "network.chat.")).read(0)).name().equals("GAME_INFO"))
				return; // It's an actionbar message, ignoring
		} else if (version.isNewerOrEquals(Version.V1_8) && packet.getBytes().readSafely(0) == (byte) 2)
			return; // It's an actionbar message, ignoring
		if (json == null || choosedGetter == null) {
			for (IComponentManager getters : componentManager) {
				String tmpJson = getters.getBaseComponentAsJSON(e);
				if (tmpJson != null) {
					json = ChatManager.fixSeparator(tmpJson);
					choosedGetter = getters;
					if (ChatManager.containsSeparator(json))
						break; // be sure it's valid one
				}
			}
		}
		if (json == null || choosedGetter == null) {
			ChatItem.debug("Can't find valid json getter or json itself");
			ChatItem.debug("String: " + packet.getStrings().getContent());
			for (Field f : e.getPacket().getClass().getDeclaredFields()) {
				f.setAccessible(true);
				try {
					ChatItem.debug(f.getName() + ": " + f.get(e.getPacket()));
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			return; // can't find something
		}
		if (!ChatManager.containsSeparator(json)) { // if the message doesn't contain the BELL separator
			ChatItem.debug("Not containing bell " + json);
			return;
		}
		ChatItem.debug("Found with " + choosedGetter.getClass().getName());
		Chat chat = choosedGetter.getChat(json);
		if (chat == null) { // something went really bad, so we run away and hide (AKA the player left or is
			// on another server)
			ChatItem.debug("Chat null for " + json);
			return;
		}
		Player itemPlayer = chat.getPlayer();
		IComponentManager getter = choosedGetter;
		e.setCancelled(true); // We cancel the packet as we're going to resends it anyways
		Bukkit.getScheduler().runTaskAsynchronously(ChatItem.getInstance(), () -> {
			Player p = e.getPlayer();
			String message = null;
			try {
				ItemStack item = ChatManager.getUsableItem(itemPlayer);
				if (!ItemUtils.isEmpty(item)) {
					ItemStack copy = item.clone();

					if (ItemPlayer.getPlayer(p).isBuggedClient()) { // if the guy that will receive it is bugged
						String act = getStorage().BUGGED_CLIENT_ACTION;
						List<String> tooltip;
						if (act.equalsIgnoreCase("tooltip"))
							tooltip = getStorage().BUGGED_CLIENTS_TOOLTIP;
						else if (act.equalsIgnoreCase("item"))
							tooltip = ChatManager.getMaxLinesFromItem(p, copy);
						else if (act.equalsIgnoreCase("show_both")) {
							tooltip = ChatManager.getMaxLinesFromItem(p, copy);
							tooltip.addAll(getStorage().BUGGED_CLIENTS_TOOLTIP);
						} else
							tooltip = new ArrayList<>();
						message = JSONManipulator.getInstance().parseEmpty(getter.getBaseComponentAsJSON(e), ChatManager.styleItem(p, copy, getStorage()), tooltip, chat.getPlayer());
						if (!manager.supportsChatComponentApi()) {
							ChatItem.debug("Use basic for 1.7 lunar");
							packet.getChatComponents().write(0, jsonToChatComponent(message));
						} else {
							ChatItem.debug("Use baseComponent for 1.7 lunar");
							packet.getSpecificModifier(BaseComponent[].class).write(0, ComponentSerializer.parse(message));
						}
						lastSentPacket = e.getPacket();
						PacketUtils.sendPacket(p, lastSentPacket);
						return;
					}
					if (copy.hasItemMeta()) {
						ItemMeta meta = copy.getItemMeta();
						if (meta instanceof BookMeta) { // filtering written books
							BookMeta bm = (BookMeta) copy.getItemMeta();
							bm.setPages(Collections.emptyList());
							copy.setItemMeta(bm);
						} else if (meta instanceof BlockStateMeta && Version.getVersion().isNewerOrEquals(Version.V1_9)) { // if it's a block
							BlockStateMeta bsm = (BlockStateMeta) copy.getItemMeta();
							if (bsm.hasBlockState() && bsm.getBlockState() instanceof ShulkerBox) {
								ShulkerBox sb = (ShulkerBox) bsm.getBlockState();
								for (ItemStack itemInv : sb.getInventory()) {
									stripData(itemInv);
								}
								bsm.setBlockState(sb);
							}
							copy.setItemMeta(bsm);
						}
					}
					lastSentPacket = getter.manageItem(p, chat, e, item, getStorage());
				} else {
					if (!getStorage().HAND_DISABLED) {
						lastSentPacket = getter.manageEmpty(p, chat, e, getStorage());
					}
				}
				if (lastSentPacket == null)
					ChatItem.debug("(v1) No packet to sent with manager " + getter.getClass().getName());
				else
					PacketUtils.sendPacket(p, lastSentPacket);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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
}
