package me.dadus33.chatitem.chatmanager.v1.packets;

import org.bukkit.entity.Player;

public class ChatItemPacket {

	protected Player player;
	protected Object packet;
	protected PacketType type;
	protected boolean cancel = false;
	
	public ChatItemPacket(PacketType type, Object packet, Player player) {
		this.player = player;
		this.packet = packet;
		this.type = type;
	}
	
	public Player getPlayer() {
		return player;
	}

	public boolean hasPlayer() {
		return player != null;
	}
	
	public String getPlayername() {
		return getPlayer().getName();
	}
	
	public Object getPacket() {
		return packet;
	}

	public String getPacketName() {
		return packet.getClass().getSimpleName();
	}
	
	public PacketType getPacketType() {
		return type;
	}
	
	public boolean isCancelled() {
		return cancel;
	}
	
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	public PacketContent getContent() {
		return new PacketContent(this);
	}
}
