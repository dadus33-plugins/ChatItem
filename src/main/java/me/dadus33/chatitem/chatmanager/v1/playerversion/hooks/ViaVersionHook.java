package me.dadus33.chatitem.chatmanager.v1.playerversion.hooks;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.chatmanager.v1.playerversion.IPlayerVersion;
import us.myles.ViaVersion.api.Via;

public class ViaVersionHook implements IPlayerVersion {

	@Override
	public int getProtocolVersion(Player p) {
		return Via.getAPI().getPlayerVersion(p.getUniqueId());
	}
}
