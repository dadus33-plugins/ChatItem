package me.dadus33.chatitem.playerversion;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.utils.ProtocolVersion;

public interface IPlayerVersion {
	
	public int getProtocolVersion(Player p);
	
	default ProtocolVersion getPlayerVersion(Player p) {
		return ProtocolVersion.getVersion(getProtocolVersion(p));
	}
}
