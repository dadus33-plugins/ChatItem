package me.dadus33.chatitem.playerversion.hooks;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playerversion.IPlayerVersion;
import us.myles.ViaVersion.api.Via;

public class ViaVersionHook implements IPlayerVersion {

	@Override
	public int getProtocolVersion(Player p) {
		return Via.getAPI().getPlayerVersion(p.getUniqueId());
	}
}
