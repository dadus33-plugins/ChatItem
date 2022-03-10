package me.dadus33.chatitem.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.collect.ImmutableList;

import me.dadus33.chatitem.ChatItem;

public class Storage {

	private FileConfiguration conf;
	public final HashMap<String, HashMap<Short, String>> TRANSLATIONS = new HashMap<>();
	public boolean COLOR_IF_ALREADY_COLORED, FORCE_ADD_AMOUNT, LET_MESSAGE_THROUGH, DENY_IF_NO_ITEM, DEBUG,
			HAND_DISABLED, SHOW_NO_PERM_NORMAL, CHECK_UPDATE, SHOW_NO_PERM_COMMAND, PERMISSION_ENABLED;
	public final String HAND_NAME, NAME_FORMAT, AMOUNT_FORMAT, MANAGER, PERMISSION_NAME, BUGGED_CLIENT_ACTION;
	public final String NO_PERMISSION_MESSAGE, DENY_MESSAGE, RELOAD_MESSAGE, COOLDOWN_MESSAGE, LIMIT_MESSAGE;
	public final String SECONDS, MINUTES, HOURS;
	public final String JOIN_UPDATE_MESSAGE, JOIN_UPDATE_HOVER;
	public int CONFIG_VERSION, LIMIT, COOLDOWN;
	public final List<Command> ALLOWED_PLUGIN_COMMANDS = new ArrayList<>();
	public final List<String> ALLOWED_DEFAULT_COMMANDS = new ArrayList<>();
	public final List<String> PLACEHOLDERS, HAND_TOOLTIP, BUGGED_CLIENTS_TOOLTIP;

	public Storage(FileConfiguration cnf) {
		this.conf = cnf;
		CONFIG_VERSION = conf.getInt("config-version", 13);
		checkConfigVersion();
		this.MANAGER = conf.getString("manager", "auto");
		Set<String> keys = conf.getConfigurationSection("translations").getKeys(false);
		for (String key : keys) {
			HashMap<Short, String> entry = new HashMap<>();
			Set<String> subKeys = conf.getConfigurationSection("translations.".concat(key)).getKeys(false);
			for (String subKey : subKeys) {
				entry.put(Short.parseShort(subKey),
						color(conf.getString("translations.".concat(key).concat(".").concat(subKey))));
			}

			TRANSLATIONS.put(key, entry);
		}
		DEBUG = conf.getBoolean("debug", false);
		PLACEHOLDERS = ImmutableList.copyOf(conf.getStringList("general.placeholders"));
		NAME_FORMAT = color(conf.getString("general.name-format", "&b&l&o{name} {amount}&r"));
		AMOUNT_FORMAT = color(conf.getString("general.amount-format", "x{times}"));
		COLOR_IF_ALREADY_COLORED = conf.getBoolean("general.color-if-already-colored", true);
		LET_MESSAGE_THROUGH = conf.getBoolean("general.let-message-through", true);
		FORCE_ADD_AMOUNT = conf.getBoolean("general.force-add-amount", true);
		DENY_IF_NO_ITEM = conf.getBoolean("general.deny-if-no-item", false);
        LIMIT = conf.getInt("general.limit", 8);
		COOLDOWN = conf.getInt("general.cooldown", 60);
		SHOW_NO_PERM_NORMAL = conf.getBoolean("general.show-no-permission-message.normal", true);
        SHOW_NO_PERM_COMMAND = conf.getBoolean("general.show-no-permission-message.command", false);
        PERMISSION_ENABLED = conf.getBoolean("general.permission.enabled", false);
        PERMISSION_NAME = conf.getString("general.permission.name", "chatitem.use");
		HAND_DISABLED = conf.getBoolean("general.hand.disabled", false);
		HAND_NAME = color(conf.getString("general.hand.name", "&b&l&o{display-name}&b&l&o's hand"));
		HAND_TOOLTIP = conf.getStringList("general.hand.tooltip").stream().map(Storage::color).collect(Collectors.toList());
		BUGGED_CLIENT_ACTION = conf.getString("general.bugged_client.action", "show_both");
		BUGGED_CLIENTS_TOOLTIP = conf.getStringList("general.bugged_client.tooltip").stream().map(Storage::color).collect(Collectors.toList());
		CHECK_UPDATE = conf.getBoolean("general.check-update", true);
		DENY_MESSAGE = color(conf.getString("messages.deny-message", "&c&lYou have no item in hand!"));
		RELOAD_MESSAGE = color(conf.getString("messages.reload-success", "&b&lSuccessful reload!"));
		NO_PERMISSION_MESSAGE = color(conf.getString("messages.no-permission",
				"&c&lI'm sorry, but you are not allowed to use the placeholder in chat!"));
		COOLDOWN_MESSAGE = color(conf.getString("messages.cooldown-message",
				"&c&lYou can only use items in chat once a minute! You have {remaining} left!"));
		JOIN_UPDATE_MESSAGE = color(conf.getString("messages.join-update.message",
				"&cA new version of ChatItem is available. &aClick here to download."));
		JOIN_UPDATE_HOVER = color(conf.getString("messages.join-update.hover", "&6Click to go to spigot page !"));
        LIMIT_MESSAGE = color(conf.getString("messages.limit-message", "&c&lYou can only add 8 item placeholders per message!"));
        
		SECONDS = color(conf.getString("messages.seconds", " seconds"));
		MINUTES = color(conf.getString("messages.minutes", " minutes"));
		HOURS = color(conf.getString("messages.hours", " hours"));
		colorStringList(HAND_TOOLTIP);
		final List<String> cmds = conf.getStringList("general.commands");
		Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), () -> {
			for (String s : cmds) {
				Command c = Bukkit.getPluginCommand(s);
				if (c != null) {
					ALLOWED_PLUGIN_COMMANDS.add(c);
				} else {
					ALLOWED_DEFAULT_COMMANDS.add(s);
				}
			}
		}, 100L);

	}

	public static String color(String s) {
		return s == null || s.isEmpty() ? s : ChatColor.translateAlternateColorCodes('&', s);
	}

	private void checkConfigVersion() {
		int latestVersion = ChatItem.CFG_VER;
		if (latestVersion != CONFIG_VERSION) {
			ChatItem pl = ChatItem.getInstance();
			pl.getLogger().warning(ChatColor.RED
					+ "ChatItem detected an older or invalid configuration file. Replacing it with the default config...");
			performOverwrite();
			conf = pl.getConfig();
			pl.getLogger().warning(ChatColor.RED + "Replacement complete!");
		}
	}

	private void performOverwrite() {
		ChatItem.getInstance().saveResource("config.yml", true);
	}

	private static void colorStringList(List<String> input) {
		for (int i = 0; i < input.size(); ++i) {
			input.set(i, color(input.get(i)));
		}
	}

}
