package me.dadus33.chatitem.hook.placeholders;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;

public class EssentialsPlaceholdersHook implements IPlaceholders {

	private Essentials getEssentials() {
		return ((Essentials) Bukkit.getPluginManager().getPlugin("Essentials"));
	}

	@SuppressWarnings("deprecation")
	@Override
	public String replace(Player p, String text) {
		User user = getEssentials().getUser(p);
		if (user == null) {
			return null;
		}
		String world = user.getWorld().getName();
		String username = user.getName();
		String nickname = user.getFormattedNickname();
		Player player = user.getBase();
		Team team = player.getScoreboard().getPlayerTeam(player);

		return text.replace("{GROUP}", user.getGroup()).replace("{WORLD}", getEssentials().getSettings().getWorldAlias(world)).replace("{WORLDNAME}", world)
				.replace("{SHORTWORLDNAME}", world.substring(0, 1).toUpperCase(Locale.ENGLISH)).replace("{TEAMNAME}", team == null ? "" : team.getDisplayName())
				.replace("{TEAMPREFIX}", team == null ? "" : team.getPrefix()).replace("{TEAMSUFFIX}", team == null ? "" : team.getSuffix())
				.replace("{PREFIX}", FormatUtil.replaceFormat(getEssentials().getPermissionsHandler().getPrefix(player)))
				.replace("{SUFFIX}", FormatUtil.replaceFormat(getEssentials().getPermissionsHandler().getSuffix(player))).replace("{USERNAME}", username)
				.replace("{NICKNAME}", nickname == null ? username : nickname);
	}

}
