package me.dadus33.chatitem.packets.protocollib;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.dadus33.chatitem.listeners.HandshakeListener;
import me.dadus33.chatitem.packets.ChatItemPacket;
import me.dadus33.chatitem.packets.PacketManager;
import me.dadus33.chatitem.packets.PacketType;

public class ProtocollibPacketManager extends PacketManager {

	private final ProtocolManager protocolManager;
	
	public ProtocollibPacketManager(Plugin pl) {
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketAdapter(pl, ListenerPriority.LOWEST, Arrays.asList(Play.Server.CHAT)) {
			@Override
			public void onPacketSending(PacketEvent e) {
				Player p = e.getPlayer();
		        if (p == null || e.isPlayerTemporary())
		        	return;
				ChatItemPacket packet = onPacketSent(PacketType.getType(e.getPacket().getHandle().getClass().getSimpleName()),
						p, e.getPacket().getHandle());
				if(packet == null)
					return;
		        if(!e.isCancelled())
		        	e.setCancelled(packet.isCancelled());
			}
		});
		protocolManager.addPacketListener(new HandshakeListener());
	}

	@Override
	public void addPlayer(Player p) {}

	@Override
	public void removePlayer(Player p) {}

	@Override
	public void clear() {}

	public ChatItemPacket onPacketSent(PacketType type, Player sender, Object packet) {
		if(type == null)
			return null;
		ChatItemPacket customPacket = new ChatItemPacket(type, packet, sender);
		notifyHandlersSent(customPacket);
		return customPacket;
	}
}
