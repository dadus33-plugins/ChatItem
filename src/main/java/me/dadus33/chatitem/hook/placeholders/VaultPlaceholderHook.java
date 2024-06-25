package me.dadus33.chatitem.hook.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;

public class VaultPlaceholderHook implements IPlaceholders {

	private Chat chat = null;

	public Chat getChat() {
		if (chat == null) {
			RegisteredServiceProvider<Chat> rs = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rs != null) // chat well setup
				chat = rs.getProvider();
		}
		return chat;
	}

	@Override
	public String replace(Player p, String text) {
		Chat chat = getChat();
		if (chat == null)
			return text;
		return text.replace("{prefix}", chat.getPlayerPrefix(p)).replace("{suffix}", chat.getPlayerSuffix(p)).replace("%prefix%", chat.getPlayerPrefix(p)).replace("%suffix%", chat.getPlayerSuffix(p));
	}

}
