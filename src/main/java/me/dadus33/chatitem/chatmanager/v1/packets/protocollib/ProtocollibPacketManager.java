package me.dadus33.chatitem.chatmanager.v1.packets.protocollib;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.listeners.HandshakeListener;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;

public class ProtocollibPacketManager extends PacketManager {

	private final ProtocolManager protocolManager;
	
	public ProtocollibPacketManager(ChatItem pl) {
		protocolManager = ProtocolLibrary.getProtocolManager();
		List<com.comphenix.protocol.PacketType> list = new ArrayList<>();
		list.add(Play.Server.CHAT);
		try {
			list.add((com.comphenix.protocol.PacketType) Play.Server.class.getDeclaredField("SYSTEM_CHAT").get(null));
		} catch (Exception e) {
			 // ignore because using old version
		}
		protocolManager.addPacketListener(new PacketAdapter(pl, ListenerPriority.LOWEST, list) {
			@Override
			public void onPacketSending(PacketEvent e) {
				Player p = e.getPlayer();
		        if (p == null || e.isPlayerTemporary())
		        	return;
				ChatItemPacket packet = onPacketSent(PacketType.getType(e.getPacket().getHandle().getClass().getSimpleName()), p, e.getPacket().getHandle());
				if(packet == null) {
					ChatItem.debug("Can't find packet: " + e.getPacket().getHandle().getClass().getName());
					return;
				}
		        if(!e.isCancelled())
		        	e.setCancelled(packet.isCancelled());
			}
		});
		protocolManager.addPacketListener(new HandshakeListener(pl));
	}
	
	@Override
	public void stop() {
		protocolManager.removePacketListeners(ChatItem.getInstance());
	}

	public ChatItemPacket onPacketSent(PacketType type, Player sender, Object packet) {
		if(type == null)
			return null;
		ChatItemPacket customPacket = new ChatItemPacket(type, packet, sender);
		notifyHandlersSent(customPacket);
		return customPacket;
	}
}
