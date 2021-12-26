package me.dadus33.chatitem.chatmanager.v1.packets;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public abstract class PacketManager {

	public abstract void addPlayer(Player p);
	public abstract void removePlayer(Player p);
	public abstract void clear();

	private final ArrayList<PacketHandler> handlers = new ArrayList<>();
	public boolean addHandler(PacketHandler handler) {
		return !handlers.add(handler);
	}

	public boolean removeHandler(PacketHandler handler) {
		return handlers.remove(handler);
	}

	public void notifyHandlersSent(ChatItemPacket packet) {
		handlers.forEach((handler) -> handler.onSend(packet));
	}
}
