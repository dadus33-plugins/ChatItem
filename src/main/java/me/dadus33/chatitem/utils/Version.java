package me.dadus33.chatitem.utils;

import java.net.InetSocketAddress;

import org.bukkit.Bukkit;

import me.dadus33.chatitem.ChatItem;

public enum Version {
	
	// http://wiki.vg/Protocol_version_numbers
	V1_7(0, 5, 7),
	V1_8(6, 47, 8),
	V1_9(49, 110, 9), // 1.9.X - Starts with 49 as 48 was an april fools update
	V1_10(201, 210, 10), // 1.10.X - Starts with 201 because why not.
	V1_11(301, 316, 11),
	V1_12(317, 340, 12),
	V1_13(341, 440, 13),
	V1_14(441, 500, 14),
	V1_15(550, 578, 15),
	V1_16(700, 754, 16),
	V1_17(755, 756, 17),
	V1_18(757, 758, 18),
	V1_19(759, 762, 19),
	V1_20(763, 1000, 20),
	HIGHER(Integer.MAX_VALUE, -1, Integer.MAX_VALUE);

	// Latest version should always have the upper limit set to Integer.MAX_VALUE so
	// I don't have to update the plugin for every minor protocol change

	public final int MIN_VER;
	public final int MAX_VER;
	public final int index; // Represents how new the version is (0 - extremely old)

	private static final Version SERVER_VERSION;
	public static final String BUKKIT_VERSION;

	static {
		BUKKIT_VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		SERVER_VERSION = getVersionByName(BUKKIT_VERSION);
	}

	Version(int min, int max, int index) {
		this.MIN_VER = min;
		this.MAX_VER = max;
		this.index = index;
	}
	
	public boolean isNewerOrEquals(Version other) {
		return index >= other.index;
	}

	public boolean isNewerThan(Version other) {
		return index > other.index;
	}

	public static Version getVersionByName(String name) {
		for (Version v : Version.values())
			if (name.toLowerCase().startsWith(v.name().toLowerCase()))
				return v;
		return HIGHER;
	}

	public static Version getVersion(int protocolVersion) {
		for (Version ver : Version.values()) {
			if (protocolVersion >= ver.MIN_VER && protocolVersion <= ver.MAX_VER) {
				return ver;
			}
		}
		return HIGHER;
	}

	public static Version getVersion(int protocolVersion, Version def) {
		for (Version ver : Version.values()) {
			if (protocolVersion >= ver.MIN_VER && protocolVersion <= ver.MAX_VER) {
				return ver;
			}
		}
		ChatItem.debug("Failed to find version for protocol " + protocolVersion);
		return def;
	}

	public static Version getVersion() {
		return SERVER_VERSION;
	}

	public static String stringifyAdress(InetSocketAddress address) {
		if(address == null)
			return "0.0.0.0:0";
		String port = String.valueOf(address.getPort());
		String ip = address.getAddress().getHostAddress();
		return ip + ":" + port;
	}
}
