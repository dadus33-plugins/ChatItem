package me.dadus33.chatitem.chatmanager.v1;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.Storage;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.listeners.ChatPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Utils;

public class PacketEditingChatManager extends ChatManager {

	private boolean baseComponentAvailable = true;
	private final ChatItemPacketManager packetManager;
	private final ChatPacketManager chatPacketManager;

	public PacketEditingChatManager(ChatItem pl) {
		packetManager = new ChatItemPacketManager(pl);
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

		PacketManager pm = packetManager.getPacketManager();
		pm.addHandler(chatPacketManager);
		Bukkit.getOnlinePlayers().forEach(pm::addPlayer); // add actual online players
	}

	@Override
	public void unload(ChatItem pl) {
		PacketManager pm = packetManager.getPacketManager();
		pm.removeHandler(chatPacketManager);
		pm.stop();
	}

	public boolean supportsChatComponentApi() {
		return baseComponentAvailable;
	}
	
	private static String checkPacketSize(String packet, int maxSize) {
		int size = packet.getBytes().length;
		while(size > maxSize) {
			packet = packet.replaceFirst("  ", " ");
			if(size == packet.getBytes().length)
				break; // could not fix myself
			size = packet.getBytes().length;
		}
		return packet;
	}

	public static Object createSystemChatPacket(String json) throws Exception {
		json = checkPacketSize(json, 150000);
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
		Class<?> packetClass = PacketUtils.getNmsClass("ClientboundPlayerChatPacket", "network.protocol.game.", "ClientboundSystemChatPacket", "PacketPlayOutChat");
		Class<?> chatMessageTypeClass = PacketUtils.isClassExist("net.minecraft.network.chat.ChatMessageType") ? PacketUtils.getNmsClass("ChatMessageType", "network.chat.") : null;
		Constructor<?> betterOne = null;
		Object[] betterParam = null;
		for (Constructor<?> cons : packetClass.getDeclaredConstructors()) {
			if (!cons.isAccessible())
				cons.setAccessible(true);

			// check for basic method
			if(obj instanceof String) {
				if (cons.getParameterCount() == 2 && cons.getParameterTypes()[0].equals(String.class) && cons.getParameterTypes()[1].equals(int.class)) { // "string, int"
					return cons.newInstance(obj, 1);
				} else if (cons.getParameterCount() == 2 && cons.getParameterTypes()[0].equals(String.class) && cons.getParameterTypes()[1].equals(boolean.class)) { // "string, boolean"
					return cons.newInstance(obj, false); // false for no overlay
				} else if (cons.getParameterCount() == 3 && cons.getParameterTypes()[1].equals(String.class)) { // "component", "string", <something not checked>
					Class<?> secondParam = cons.getParameterTypes()[2];
					if (secondParam.equals(int.class)) // "component", "string", "int"
						return cons.newInstance(null, obj, 1);
					else if (secondParam.equals(boolean.class)) // "component", "string", "boolean"
						return cons.newInstance(null, obj, false);
				}
			}
			
			int nbPut = 0;
			Object[] params = new Object[cons.getParameterCount()];
			for(int i = 0; i < params.length; i++) {
				if(cons.getParameterTypes()[i].isAssignableFrom(obj.getClass())) {
					params[i] = obj;
					nbPut++;
				} else if(chatMessageTypeClass != null && cons.getParameterTypes()[i].isAssignableFrom(chatMessageTypeClass)) {
					params[i] = getChatMessageType();
				} else if(cons.getParameterTypes()[i].isAssignableFrom(boolean.class)) // need to set primitives
					params[i] = false;
				else if(cons.getParameterTypes()[i].isAssignableFrom(int.class)) // need to set primitives
					params[i] = 0;
			}
			if(nbPut == 1) {
				if((betterOne == null && betterParam == null) || betterParam.length > params.length) {
					betterOne = cons;
					betterParam = params;
					if(params.length == 1) // only what we need
						break;
					continue; // check for something better?
				}
			} else if(nbPut > 1)
				ChatItem.getInstance().getLogger().warning("Some constructor seems to have too many " + obj.getClass().getSimpleName() + ". Class: " + packetClass.getSimpleName());
		}
		if(betterOne != null && betterParam != null) {
			try {
				return betterOne.newInstance(betterParam);
			} catch (IllegalArgumentException e) {
				ChatItem.getInstance().getLogger().warning("Failed to create a new packet to send. " + betterOne.getParameterCount() + " (" + Utils.arrayToString(betterOne.getParameterTypes()) + "). Params: " + Utils.arrayToString(betterParam));
				return null;
			}
		}
		return null;
	}
	
	private static Object getChatMessageType() throws Exception {
		Class<?> c = PacketUtils.getNmsClass("ChatMessageType", "network.chat.");
		return ReflectionUtils.getMethod(c, c, byte.class).invoke(null, (byte) 0);
	}
}
