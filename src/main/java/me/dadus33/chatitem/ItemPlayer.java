package me.dadus33.chatitem;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.dadus33.chatitem.playerversion.PlayerVersionManager;
import me.dadus33.chatitem.utils.Version;

public class ItemPlayer {

	private static final ConcurrentHashMap<UUID, ItemPlayer> PLAYERS = new ConcurrentHashMap<>();
	public static ItemPlayer getPlayer(Player p) {
		return PLAYERS.computeIfAbsent(p.getUniqueId(), ItemPlayer::new);
	}
	public static ItemPlayer getPlayer(UUID uuid) {
		return PLAYERS.computeIfAbsent(uuid, ItemPlayer::new);
	}
	private final UUID uuid;
	private int protocolVersion = 0;
	private Version version = null;
	private String clientName = "unknow";
	
	public ItemPlayer(UUID uuid) {
		this.uuid = uuid;
		Player p = Bukkit.getPlayer(uuid);
		if(p != null)
			setVersion(PlayerVersionManager.getPlayerVersionAdapter().getPlayerVersion(p));
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public int getProtocolVersion() {
		if(protocolVersion == 0)
			setProtocolVersion(PlayerVersionManager.getPlayerVersionAdapter().getProtocolVersion(getPlayer()));
		return protocolVersion;
	}
	
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
		if(version == Version.getVersion()) {// if is default
			version = Version.getVersion(protocolVersion);
			ChatItem.debug("Detected version " + version.name() + " (protocol: " + protocolVersion + ")");
		}
	}
	
	public Version getVersion() {
		if(version == null)
			setVersion(PlayerVersionManager.getPlayerVersionAdapter().getPlayerVersion(getPlayer()));
		return version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
		if(protocolVersion == 0)
			this.protocolVersion = version.MAX_VER;
	}
	
	public boolean isBuggedClient() {
		return (getVersion().equals(Version.V1_7) && getClientName().toLowerCase().contains("lunarclient")) || (!getVersion().isNewerOrEquals(Version.V1_15) && Version.getVersion().isNewerOrEquals(Version.V1_16));
	}
	
	@Override
	public String toString() {
		return "ItemPlayer[uuid=" + uuid + ",name=" + (getPlayer() != null ? getPlayer().getName() : "-") + ",version=" + version.name() + ",protocol=" + protocolVersion + ",client=" + clientName + "]";
	}
}
