package me.dadus33.chatitem.playerversion.hooks;

import java.util.HashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;

import me.dadus33.chatitem.listeners.HandshakeListener;
import me.dadus33.chatitem.playerversion.IPlayerVersion;
import me.dadus33.chatitem.utils.ProtocolVersion;

public class DefaultVersionHook implements IPlayerVersion {

	public final HashMap<String, Integer> protocolPerUUID = new HashMap<>();
	
	public DefaultVersionHook() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new HandshakeListener(this));
	}
	
	@Override
	public int getProtocolVersion(Player p) {
		return protocolPerUUID.getOrDefault(ProtocolVersion.stringifyAdress(p.getAddress()), ProtocolVersion.getServerVersion().MAX_VER);
	}
}
