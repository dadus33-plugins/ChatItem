package me.dadus33.chatitem.playerversion.hooks;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.playerversion.IPlayerVersion;
import me.dadus33.chatitem.utils.Version;

public class DefaultVersionHook implements IPlayerVersion {

	@Override
	public int getProtocolVersion(Player p) {
		return Version.getVersion().MAX_VER;
	}
}
