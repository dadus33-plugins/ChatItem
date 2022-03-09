package me.dadus33.chatitem.playerversion.hooks;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playerversion.IPlayerVersion;
import protocolsupport.api.ProtocolSupportAPI;

public class ProtocolSupportHook implements IPlayerVersion {
	
	@Override
	public int getProtocolVersion(Player p) {
		return ProtocolSupportAPI.getProtocolVersion(p).getId();
	}

}
