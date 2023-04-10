package me.dadus33.chatitem.chatmanager.v1;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatEventListener;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketManager;
import me.dadus33.chatitem.utils.PacketUtils;

public class PacketEditingChatManager extends ChatManager {

	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatEventListener chatEventListener;
	private final ChatPacketManager chatPacketManager;

	public PacketEditingChatManager(ChatItem pl) {
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

	public boolean supportsChatComponentApi() {
		return baseComponentAvailable;
	}

	public static Object createSystemChatPacket(String json) throws Exception {
		Object packet = internalCreateSystemChatPacket(json);
		if(packet != null)
			return packet;
		packet = internalCreateSystemChatPacket(PacketUtils.ICB_FROM_JSON.invoke(null, json));
		if(packet != null)
			return packet;
		ChatItem.getInstance().getLogger().warning("Can't create a new packet for json " + json);
		return null;
	}
	
	private static Object internalCreateSystemChatPacket(Object obj) throws Exception {
		Class<?> packetClass = PacketUtils.getNmsClass("ClientboundSystemChatPacket", "network.protocol.game.", "PacketPlayOutChat");
		for (Constructor<?> cons : packetClass.getDeclaredConstructors()) {
			if(cons.getParameterCount() == 1) // only the serializer thing
				continue;
			if (!cons.isAccessible())
				cons.setAccessible(true);
			int nbPut = 0;
			Object[] params = new Object[cons.getParameterCount()];
			for(int i = 0; i < params.length; i++) {
				if(cons.getParameterTypes()[i].getClass().equals(String.class)) {
					params[i] = obj;
					nbPut++;
				}
			}
			if(nbPut == 1)
				return cons.newInstance(params);
			else if(nbPut > 1)
				ChatItem.getInstance().getLogger().warning("Some constructor seems to have too many " + obj.getClass().getSimpleName() + ". Class: " + packetClass.getSimpleName());

			// now check for basic method
			if(obj instanceof String) {
				if (cons.getParameterCount() == 2 && cons.getParameterTypes()[0].equals(String.class) && cons.getParameterTypes()[1].equals(int.class)) { // "string, int"
					return cons.newInstance(obj, 1);
				} else if (cons.getParameterCount() == 3 && cons.getParameterTypes()[1].equals(String.class)) { // "component", "string", <something not checked>
					Class<?> secondParam = cons.getParameterTypes()[2];
					if (secondParam.equals(int.class)) // "component", "string", "int"
						return cons.newInstance(null, obj, 1);
					else if (secondParam.equals(boolean.class)) // "component", "string", "boolean"
						return cons.newInstance(null, obj, false);
				}
			}
		}
		return null;
	}
}
