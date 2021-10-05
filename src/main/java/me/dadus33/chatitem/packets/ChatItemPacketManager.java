package me.dadus33.chatitem.packets;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.packets.custom.CustomPacketManager;
import me.dadus33.chatitem.packets.protocollib.ProtocollibPacketManager;

public class ChatItemPacketManager {

	private PacketManager packetManager;
	
	public ChatItemPacketManager(ChatItem pl) {
		Plugin protocolLibPlugin = Bukkit.getPluginManager().getPlugin("ProtocolLib");
		if (protocolLibPlugin != null) {
			if(checkProtocollibConditions()) {
				pl.getLogger().info("The plugin ProtocolLib has been detected. Loading Protocollib support ...");
				packetManager = new ProtocollibPacketManager(pl);
			} else {
				pl.getLogger().warning("The plugin ProtocolLib has been detected but you have an OLD version, so we cannot use it.");
				pl.getLogger().warning("Fallback to default Packet system ...");
				packetManager = new CustomPacketManager(pl);
			}
		} else
			packetManager = new CustomPacketManager(pl);
	}
	
	public PacketManager getPacketManager() {
		return packetManager;
	}
	
	private boolean checkProtocollibConditions() {
		try {
			Class.forName("com.comphenix.protocol.injector.server.TemporaryPlayer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
