package me.dadus33.chatitem.packets.protocollib;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketEvent;

import me.dadus33.chatitem.packets.AbstractPacket;
import me.dadus33.chatitem.packets.PacketType;

public class ProtocollibPacket extends AbstractPacket {
	
	private PacketEvent event;
	
	public ProtocollibPacket(PacketType type, Object packet, Player p, PacketEvent event) {
		super(type, packet, p);
		this.event = event;
	}
	
	public PacketEvent getProtocollibEvent() {
		return event;
	}
}
