package me.dadus33.chatitem.chatmanager.v1.packets;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.CustomPacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.protocollib.ProtocollibPacketManager;

public class ChatItemPacketManager {

	private PacketManager packetManager;
	
	public ChatItemPacketManager(ChatItem pl) {
		Plugin protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
		if (protocolLibPlugin != null) {
			String version = protocolLibPlugin.getDescription().getVersion();
			if(version.contains("5.0.0-SNAPSHOT")) {
				pl.getLogger().warning("ProtocolLib have been detected, but actually, snapshot have issues with chat packet. More informations here: https://github.com/dmulloy2/ProtocolLib/issues/1670.");
				packetManager = new CustomPacketManager(pl);
			} else if(checkProtocollibConditions()) {
				pl.getLogger().info("The plugin ProtocolLib has been detected. Loading Protocollib support ...");
				packetManager = new ProtocollibPacketManager(pl);
			} else {
				pl.getLogger().warning("The plugin ProtocolLib has been detected but you have an old or too new version, so we cannot use it.");
				pl.getLogger().warning("Fallback to default Packet system ...");
				packetManager = new CustomPacketManager(pl);
			}
		} else {
			pl.getLogger().info("Loading own packet system.");
			packetManager = new CustomPacketManager(pl);
		}
	}
	
	public PacketManager getPacketManager() {
		return packetManager;
	}
	
	private boolean checkProtocollibConditions() {
		for(String searchedClass : Arrays.asList("com.comphenix.protocol.injector.server.TemporaryPlayer", "com.comphenix.protocol.injector.temporary.TemporaryPlayer")) { // class since 4.4.0 until 4.8.0, then the new one
			try {
				Class.forName(searchedClass);
				return true; // class found
			} catch (ClassNotFoundException e) {}
		}
		return false;
	}
}
