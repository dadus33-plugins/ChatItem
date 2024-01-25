package me.dadus33.chatitem.chatmanager.v1.listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.Chat;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.IComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.AdventureComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.ComponentNMSManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.PCMComponentManager;
import me.dadus33.chatitem.chatmanager.v1.basecomp.hook.StringComponentManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketHandler;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.utils.ItemUtils;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Version;

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

		for (IComponentManager getter : Arrays.asList(new StringComponentManager(), new ComponentNMSManager(), new PCMComponentManager())) {
			tryRegister(getter);
		}
		try {
			Class.forName("net.kyori.adventure.text.Component");
			tryRegister(new AdventureComponentManager());
		} catch (Exception e) {}
		ChatItem.getInstance().getLogger().info("Loaded " + componentManager.size() + " getter for base components.");
		ChatItem.debug("ComponentManager: " + String.join(", ", componentManager.stream().map(IComponentManager::getClass).map(Class::getSimpleName).collect(Collectors.toList())));
	}

	private void tryRegister(IComponentManager getter) {
		if (getter.hasConditions())
			componentManager.add(getter);
		else
			ChatItem.debug("Component " + getter.getClass().getSimpleName() + " doesn't answer conditions");
	}

	@Override
	public void onSend(ChatItemPacket e) {
		if (!e.hasPlayer() || !e.getPacketType().equals(PacketType.Server.CHAT))
			return;
		if(ChatManager.isTestingEnabled() && !ChatManager.isTesting("packet"))
			return;
		if (lastSentPacket != null && lastSentPacket == e.getPacket())
			return; // prevent infinite loop
		ChatItem.debug("Checking: " + e.getPacket().getClass().getSimpleName() + " to " + e.getPlayername());
		PacketContent packet = e.getContent();
		Version version = Version.getVersion();
		String json = "{}";
		IComponentManager choosedGetter = null;
		if (version.isNewerOrEquals(Version.V1_19)) {
			/*if (packet.getIntegers().readSafely(0, 0) > 1) { // not parsed chat message type
				ChatItem.debug("Invalid int: " + packet.getIntegers().read(0));
				return;
			}*/
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
				} else
					ChatItem.debug("Null JSON for manager " + getters.getClass().getSimpleName());
			}
		}
		if (json == null || choosedGetter == null) {
			ChatItem.debug("Can't find valid json getter or json itself");
			ChatItem.debug("String: " + packet.getStrings().getContent());
			PacketUtils.printPacketToDebug(e.getPacket());
			return; // can't find something
		}
		if (!ChatManager.containsSeparator(json)) // if the message doesn't contain the BELL separator
			return;
		ChatItem.debug("Found with " + choosedGetter.getClass().getName());
		Chat chat = choosedGetter.getChat(json);
		if (chat == null) { // something went really bad, so we run away and hide (AKA the player left or is
			// on another server)
			ChatItem.debug("Chat null for " + json);
			return;
		}
		Player itemPlayer = chat.getPlayer();
		if (getStorage().cooldown > 0 && !itemPlayer.hasPermission("chatitem.ignore-cooldown"))
			ChatManager.applyCooldown(itemPlayer);
		IComponentManager getter = choosedGetter;
		String fjson = json;
		ChatItem.debug("Final json used: " + fjson);
		e.setCancelled(true); // We cancel the packet as we're going to resends it anyways
		CompletableFuture.runAsync(() -> {
			Player p = e.getPlayer();
			String message = null;
			try {
				ItemStack item = ChatManager.getUsableItem(itemPlayer, chat.getSlot());
				if (!ItemUtils.isEmpty(item)) {
					ItemStack copy = item.clone();

					if (ItemPlayer.getPlayer(p).isBuggedClient()) { // if the guy that will receive it is bugged
						String act = getStorage().buggedClientAction;
						List<String> tooltip;
						if (act.equalsIgnoreCase("tooltip"))
							tooltip = getStorage().tooltipBuggedClient;
						else if (act.equalsIgnoreCase("item"))
							tooltip = ChatManager.getMaxLinesFromItem(p, copy);
						else if (act.equalsIgnoreCase("show_both")) {
							tooltip = ChatManager.getMaxLinesFromItem(p, copy);
							tooltip.addAll(getStorage().tooltipBuggedClient);
						} else
							tooltip = new ArrayList<>();
						message = JSONManipulator.getInstance().parseEmpty(getter.getBaseComponentAsJSON(e), ChatManager.styleItem(p, copy, getStorage()), tooltip, chat.getPlayer());
						if(message != null) {
							getter.writeJson(e, message);
						}
						lastSentPacket = e.getPacket();
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
					lastSentPacket = getter.manageItem(p, chat, e, item, fjson, getStorage());
				} else {
					if (!getStorage().handDisabled) {
						lastSentPacket = getter.manageEmpty(p, chat, e, fjson, getStorage());
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
