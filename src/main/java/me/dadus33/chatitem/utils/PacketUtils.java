package me.dadus33.chatitem.utils;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.chatmanager.v1.utils.ProtocolVersion;

public class PacketUtils {

	public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
			.split(",")[3];
	public static final String NMS_PREFIX;
	public static final String OBC_PREFIX;
	public static final boolean IS_THERMOS;
	public static final Class<?> CRAFT_PLAYER_CLASS, CRAFT_SERVER_CLASS, CHAT_SERIALIZER, COMPONENT_CLASS;
	
	/**
	 * This Map is to reduce Reflection action which take more resources than just RAM action
	 */
	private static final HashMap<String, Class<?>> ALL_CLASS;
	
	static {
		ALL_CLASS = new HashMap<>();
		IS_THERMOS = isClassExist("thermos.Thermos");
		NMS_PREFIX = Version.getVersion().isNewerOrEquals(Version.V1_17) || IS_THERMOS ? "net.minecraft." : "net.minecraft.server." + VERSION + ".";
		OBC_PREFIX = "org.bukkit.craftbukkit." + VERSION + ".";
		CRAFT_PLAYER_CLASS = getObcClass("entity.CraftPlayer");
		CHAT_SERIALIZER = getNmsClass("IChatBaseComponent$ChatSerializer", "network.chat.", "ChatSerializer");
		COMPONENT_CLASS = getNmsClass("IChatBaseComponent", "network.chat.");
		CRAFT_SERVER_CLASS = getObcClass("CraftServer");
	}
	
	/**
	 * Get the Class in NMS, with a processing reducer
	 * 
	 * @param name of the NMS class (in net.minecraft.server package ONLY, because it's NMS)
	 * @return clazz the searched class
	 */
	public static Class<?> getNmsClass(String name, String packagePrefix, String... alias){
		return ALL_CLASS.computeIfAbsent(name, (a) -> {
			String fullPrefix = NMS_PREFIX + (Version.getVersion().isNewerOrEquals(Version.V1_17) || IS_THERMOS ? packagePrefix : "");
			try {
				Class<?> clazz = Class.forName(fullPrefix + name);
				if(clazz != null)
					return clazz;
			} catch (ClassNotFoundException e) {
				if(alias.length == 0)
					e.printStackTrace(); // no alias, print error
				// else ignore and go check for alias
			}
			
			for(String className : alias) {
				try {
					Class<?> clazz = Class.forName(fullPrefix + className);
					if(clazz != null)
						return clazz;
				} catch (ClassNotFoundException e) {
					if(className == alias[alias.length - 1]) // if it's last alias, print error
						e.printStackTrace();
				}
			}
			return null;
		});
	}
		
	/**
	 * Get the Class in NMS, with a processing reducer
	 * 
	 * @param name of the NMS class (in net.minecraft.server package ONLY, because it's NMS)
	 * @return clazz the searched class
	 */
	public static Class<?> getObcClass(String name, String... alias){
		return ALL_CLASS.computeIfAbsent(name, (a) -> {
			try {
				Class<?> clazz = Class.forName(OBC_PREFIX + name);
				if(clazz != null)
					return clazz;
			} catch (ClassNotFoundException e) {
				if(alias.length == 0)
					e.printStackTrace(); // no alias, print error
				// else ignore and go check for alias
			}
			
			for(String className : alias) {
				try {
					Class<?> clazz = Class.forName(OBC_PREFIX + className);
					if(clazz != null)
						return clazz;
				} catch (ClassNotFoundException e) {
					if(className == alias[alias.length - 1]) // if it's last alias, print error
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
			Object craftPlayer = CRAFT_PLAYER_CLASS.cast(p);
			return craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
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
			if(ProtocolVersion.getServerVersion().isNewerOrEquals(ProtocolVersion.V1_17))
				return entityPlayer.getClass().getField("b").get(entityPlayer);
			else
				return entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
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
			Object craftServer = CRAFT_SERVER_CLASS.cast(Bukkit.getServer());
			return craftServer.getClass().getMethod("getHandle").invoke(craftServer);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Send the packet to the specified player
	 * 
	 * @param p which will receive the packet
	 * @param packet the packet to sent
	 */
	public static void sendPacket(Player p, Object packet) {
		try {
			Object playerConnection = getPlayerConnection(p);
			playerConnection.getClass().getMethod("sendPacket", getNmsClass("Packet", "network.protocol.")).invoke(playerConnection, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
