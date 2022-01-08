package me.dadus33.chatitem.chatmanager.v1.packets.custom.channel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.chatmanager.v1.packets.custom.CustomPacketManager;
import me.dadus33.chatitem.chatmanager.v1.playerversion.hooks.DefaultVersionHook;

public abstract class ChannelAbstract {

	private ExecutorService addChannelExecutor, removeChannelExecutor;
	static final String KEY_HANDSHAKE = "packet_handshake_chatitem", KEY_HANDLER_SERVER = "packet_handler", KEY_SERVER = "packet_server_chatitem";

	private CustomPacketManager customPacketManager;
	private HashSet<UUID> players = new HashSet<>();
	
	protected ChannelAbstract(CustomPacketManager customPacketManager) {
		this.customPacketManager = customPacketManager;
	}
	
	public CustomPacketManager getPacketManager() {
		return customPacketManager;
	}
	
	public ExecutorService getAddChannelExecutor() {
		return addChannelExecutor;
	}
	
	public ExecutorService getOrCreateAddChannelExecutor() {
		if(addChannelExecutor == null)
			addChannelExecutor = Executors.newSingleThreadExecutor();
		return addChannelExecutor;
	}
	
	public ExecutorService getRemoveChannelExecutor() {
		return removeChannelExecutor;
	}
	
	public ExecutorService getOrCreateRemoveChannelExecutor() {
		if(removeChannelExecutor == null)
			removeChannelExecutor = Executors.newSingleThreadExecutor();
		return removeChannelExecutor;
	}

	public void addPlayer(Player p) {
		if(players.add(p.getUniqueId())) {
			addChannel(p, p.getUniqueId().toString());
			try {
				Integer protocol = customPacketManager.protocolVersionPerChannel.remove(getChannel(p));
				if(protocol != null)
					DefaultVersionHook.PROTOCOL_PER_UUID.put(p.getUniqueId(), protocol);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void removePlayer(Player p) {
		if(players.remove(p.getUniqueId()))
			removeChannel(p, p.getUniqueId().toString());
	}
	
	public void removeAllPlayers() {
		new ArrayList<>(players).forEach((uuid) -> {
			Player p = Bukkit.getPlayer(uuid);
			if(p != null) // player don't left
				removeChannel(Bukkit.getPlayer(uuid), uuid.toString());
		});
		players.clear();
	}
	
	public abstract void addChannel(Player player, String endChannelName);

	public abstract void removeChannel(Player player, String endChannelName);
	
	public abstract Object getChannel(Player p) throws Exception;

}
