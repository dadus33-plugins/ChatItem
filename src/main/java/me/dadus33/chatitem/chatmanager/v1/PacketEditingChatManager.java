package me.dadus33.chatitem.chatmanager.v1;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulatorCurrent;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.Storage;

public class PacketEditingChatManager extends ChatManager {

	private final JSONManipulatorCurrent jsonManipulator;
	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatEventListener chatEventListener;
	private final ChatPacketManager chatPacketManager;

	public PacketEditingChatManager(ChatItem pl) {
		jsonManipulator = new JSONManipulatorCurrent();
		packetManager = new ChatItemPacketManager(pl);
		chatEventListener = new ChatEventListener(this);
		chatPacketManager = new ChatPacketManager(this);

		// Check for existence of BaseComponent class (only on spigot)
		try {
			Class.forName("net.md_5.bungee.api.chat.BaseComponent");
		} catch (ClassNotFoundException e) {
			baseComponentAvailable = false;
		}
	}

	@Override
	public String getName() {
		return "PacketEditing";
	}

	@Override
	public String getId() {
		return "packet";
	}

	@Override
	public void load(ChatItem pl, Storage s) {
		super.load(pl, s);

		Bukkit.getPluginManager().registerEvents(chatEventListener, pl);
		PacketManager pm = packetManager.getPacketManager();
		pm.addHandler(chatPacketManager);
		Bukkit.getOnlinePlayers().forEach(pm::addPlayer); // add actual online players
	}

	@Override
	public void unload(ChatItem pl) {
		HandlerList.unregisterAll(chatEventListener);
		PacketManager pm = packetManager.getPacketManager();
		pm.removeHandler(chatPacketManager);
		pm.stop();
	}

	public JSONManipulatorCurrent getManipulator() {
		return jsonManipulator;
	}

	public boolean supportsChatComponentApi() {
		return baseComponentAvailable;
	}

	public static Object createSystemChatPacket(String json) throws Exception {
		Class<?> packetClass = PacketUtils.getNmsClass("ClientboundSystemChatPacket", "network.protocol.game.");
		for (Constructor<?> cons : packetClass.getDeclaredConstructors()) {
			if (!cons.isAccessible())
				cons.setAccessible(true);
			if (cons.getParameterCount() == 2 && cons.getParameterTypes()[0].equals(String.class)
					&& cons.getParameterTypes()[1].equals(int.class)) { // "string, int"
				return cons.newInstance(json, 1);
			} else if (cons.getParameterCount() == 3 && cons.getParameterTypes()[1].equals(String.class)
					&& cons.getParameterTypes()[2].equals(int.class)) { // "component", "string", "int"
				return cons.newInstance(null, json, 1);
			}
		}
		ChatItem.getInstance().getLogger()
				.warning("Can't create a new packet for json " + json + ": no constructor found.");
		return null;
	}
}
