package me.dadus33.chatitem.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.json.JSONManipulator;

public class PacketUtils {

	public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	public static final String NMS_PREFIX;
	public static final String OBC_PREFIX;
	public static final boolean IS_THERMOS;
	public static final Class<?> CHAT_SERIALIZER, COMPONENT_CLASS;
	public static final Method ICB_FROM_JSON;

	/**
	 * This Map is to reduce Reflection action which take more resources than just
	 * RAM action
	 */
	private static final Map<String, Class<?>> ALL_CLASS = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	static {
		IS_THERMOS = isClassExist("thermos.Thermos");
		NMS_PREFIX = Version.getVersion().isNewerOrEquals(Version.V1_17) || IS_THERMOS ? "net.minecraft." : "net.minecraft.server." + VERSION + ".";
		OBC_PREFIX = "org.bukkit.craftbukkit." + VERSION + ".";
		CHAT_SERIALIZER = getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer");
		COMPONENT_CLASS = getNmsClass("IChatBaseComponent", "network.chat.");
		ICB_FROM_JSON = getMethod("a", CHAT_SERIALIZER, String.class);
	}

	/**
	 * Get the Class in NMS, with a processing reducer
	 * 
	 * @param name of the NMS class (in net.minecraft.server package ONLY, because it's NMS)
	 * @return clazz the searched class
	 */
	public static Class<?> getNmsClass(String name, String packagePrefix, String... alias) {
		return ALL_CLASS.computeIfAbsent(name, (a) -> {
			String fullPrefix = NMS_PREFIX + (Version.getVersion().isNewerOrEquals(Version.V1_17) || IS_THERMOS ? packagePrefix : "");
			try {
				Class<?> clazz = Class.forName(fullPrefix + name);
				if (clazz != null)
					return clazz;
			} catch (ClassNotFoundException e) {
				if (alias.length == 0)
					e.printStackTrace(); // no alias, print error
				// else ignore and go check for alias
			}

			for (String className : alias) {
				try {
					Class<?> clazz = Class.forName(fullPrefix + className);
					if (clazz != null)
						return clazz;
				} catch (ClassNotFoundException e) {
					if (className == alias[alias.length - 1]) // if it's last alias, print error
						e.printStackTrace();
				}
			}
			return null;
		});
	}

	/**
	 * Get the Class in NMS, with a processing reducer
	 * 
	 * @param name of the NMS class (in net.minecraft.server package ONLY, because
	 *             it's NMS)
	 * @return clazz the searched class
	 */
	public static Class<?> getObcClass(String name, String... alias) {
		return ALL_CLASS.computeIfAbsent(name, (a) -> {
			try {
				Class<?> clazz = Class.forName(OBC_PREFIX + name);
				if (clazz != null)
					return clazz;
			} catch (ClassNotFoundException e) {
				if (alias.length == 0)
					e.printStackTrace(); // no alias, print error
				// else ignore and go check for alias
			}

			for (String className : alias) {
				try {
					Class<?> clazz = Class.forName(OBC_PREFIX + className);
					if (clazz != null)
						return clazz;
				} catch (ClassNotFoundException e) {
					if (className == alias[alias.length - 1]) // if it's last alias, print error
						e.printStackTrace();
				}
			}
			return null;
		});
	}

	/**
	 * Get NMS entity player of specified one
	 * 
	 * @param p the player that we want the NMS entity player
	 * @return the entity player
	 */
	public static Object getEntityPlayer(Player p) {
		try {
			return getObcClass("entity.CraftPlayer").getMethod("getHandle").invoke(p);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean isClassExist(String name) {
		try {
			Class.forName(name);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Get NMS player connection of specified player
	 * 
	 * @param p Player of which we want to get the player connection
	 * @return the NMS player connection
	 */
	public static Object getPlayerConnection(Player p) {
		try {
			Object entityPlayer = getEntityPlayer(p);
			return ReflectionUtils.getFirstWith(entityPlayer, entityPlayer.getClass(), getNmsClass("PlayerConnection", "server.network."));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get NMS entity player of specified one
	 * 
	 * @param p the player that we want the NMS entity player
	 * @return the entity player
	 */
	public static Object getCraftServer() {
		try {
			return getObcClass("CraftServer").getMethod("getHandle").invoke(Bukkit.getServer());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Send the packet to the specified player
	 * 
	 * @param p      which will receive the packet
	 * @param packet the packet to sent
	 */
	public static void sendPacket(Player p, Object packet) {
		try {
			Object playerConnection = getPlayerConnection(p);
			playerConnection.getClass().getMethod(Version.getVersion().isNewerOrEquals(Version.V1_18) ? "a" : "sendPacket", getNmsClass("Packet", "network.protocol."))
					.invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Method getNbtMethod;

	/**
	 * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string for
	 * sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
	 *
	 * @param item the item to convert
	 * @return the Json string representation of the item
	 */
	public static String getNbtTag(ItemStack item) {
		try {
			if (getNbtMethod == null) {
				Class<?> itemClass = getNmsClass("ItemStack", "world.item.");
				Version v = Version.getVersion();
				if (v.isNewerOrEquals(Version.V1_20))
					getNbtMethod = itemClass.getDeclaredMethod("w");
				else if (v.equals(Version.V1_19))
					getNbtMethod = itemClass.getDeclaredMethod("u");
				else if (v.equals(Version.V1_18))
					getNbtMethod = itemClass.getDeclaredMethod("t");
				else
					getNbtMethod = itemClass.getDeclaredMethod("getTag");

				getNbtMethod.setAccessible(true);
			}
			Object tag = getNbtMethod.invoke(JSONManipulator.AS_NMS_COPY.invoke(null, item));
			return tag == null ? "{}" : tag.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

	private static Method getMethod(String name, Class<?> clazz, Class<?>... params) {
		try {
			return clazz.getDeclaredMethod(name, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void printPacketToDebug(Object packet) {
		if(!ChatItem.hasDebug())
			return;
		if(packet == null) {
			ChatItem.debug("Packet is null");
			return;
		}
		ChatItem.debug("Packet with Class: " + packet.getClass().getSimpleName());
		for (Field f : packet.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			try {
				ChatItem.debug(f.getName() + ": " + f.get(packet));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
