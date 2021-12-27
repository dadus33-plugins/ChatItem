package me.dadus33.chatitem.chatmanager.v1.packets.custom;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketManager;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.channel.ChannelAbstract;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.channel.INC2Channel;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.channel.INCChannel;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.channel.NMUChannel;
import me.dadus33.chatitem.utils.ProtocolVersion;
import me.dadus33.chatitem.utils.Utils;

public class CustomPacketManager extends PacketManager implements Listener {
	
	private ChannelAbstract channel;
	private Plugin pl;
	public HashMap<Object, Integer> protocolVersionPerChannel = new HashMap<>();
	private boolean isStarted = false;

	public CustomPacketManager(Plugin pl) {
		this.pl = pl;
		ProtocolVersion version = ProtocolVersion.getServerVersion();
		if (version.isNewerOrEquals(ProtocolVersion.V1_17))
			channel = new INC2Channel(this);
		else if (version.equals(ProtocolVersion.V1_7))
			channel = new NMUChannel(this);
		else
			channel = new INCChannel(this);
		pl.getServer().getPluginManager().registerEvents(this, pl);
		
		// we wait the start server
		Bukkit.getScheduler().runTaskLater(pl, new Runnable() {
			@Override
			public void run() {
				isStarted = true;
				for(Player p : Utils.getOnlinePlayers())
					addPlayer(p);
			}
		}, 40);
	}
	
	public Plugin getPlugin() {
		return pl;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		addPlayer(e.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removePlayer(e.getPlayer());
	}

	@Override
	public void addPlayer(Player p) {
		if(isStarted)
			channel.addPlayer(p);
	}

	@Override
	public void removePlayer(Player p) {
		channel.removePlayer(p);
	}

	@Override
	public void clear() {
		for(Player player : Utils.getOnlinePlayers())
			removePlayer(player);
		if(channel.getAddChannelExecutor() != null)
			channel.getAddChannelExecutor().shutdownNow();
		if(channel.getRemoveChannelExecutor() != null)
			channel.getRemoveChannelExecutor().shutdownNow();
	}

	public ChatItemPacket onPacketSent(PacketType type, Player sender, Object packet) {
		if(type == null) {
			ChatItem.debug("Failed to find packet type for " + packet.getClass().getSimpleName());
			return null;
		}
		ChatItemPacket customPacket = new ChatItemPacket(type, packet, sender);
		notifyHandlersSent(customPacket);
		return customPacket;
	}
}
