package me.dadus33.chatitem.playerversion.hooks;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playerversion.IPlayerVersion;
import me.dadus33.chatitem.utils.Version;

public class DefaultVersionHook implements IPlayerVersion {

	public static final HashMap<String, Integer> PROTOCOL_PER_ADDRESS = new HashMap<>();
	
	@Override
	public int getProtocolVersion(Player p) {
		return PROTOCOL_PER_ADDRESS.getOrDefault(Version.stringifyAdress(p.getAddress()), Version.getVersion().MAX_VER);
	}
}
