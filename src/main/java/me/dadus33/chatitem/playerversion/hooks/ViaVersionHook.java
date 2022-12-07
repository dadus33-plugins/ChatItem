package me.dadus33.chatitem.playerversion.hooks;

import org.bukkit.entity.Player;

import com.viaversion.viaversion.api.Via;

import me.dadus33.chatitem.playerversion.IPlayerVersion;

public class ViaVersionHook implements IPlayerVersion {

	@Override
	public int getProtocolVersion(Player p) {
		return Via.getAPI().getPlayerVersion(p.getUniqueId());
	}
}
