package me.dadus33.chatitem.packets.custom;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.packets.AbstractPacket;
import me.dadus33.chatitem.packets.PacketType;

public class CustomPacket extends AbstractPacket {
	
	public CustomPacket(PacketType type, Object packet, Player p) {
		super(type, packet, p);
	}
}