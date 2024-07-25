package me.dadus33.chatitem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Strings;

import me.dadus33.chatitem.chatmanager.ChatEventListener;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v2.ChatListenerChatManager;
import me.dadus33.chatitem.chatmanager.v3.PaperChatManager;
import me.dadus33.chatitem.commands.CIReloadCommand;
import me.dadus33.chatitem.commands.ChatItemCommand;
import me.dadus33.chatitem.hook.ChatControlSupport;
import me.dadus33.chatitem.hook.ChatManagerSupport;
import me.dadus33.chatitem.hook.ecoenchants.EcoEnchantsSupport;
import me.dadus33.chatitem.hook.placeholders.IPlaceholders;
import me.dadus33.chatitem.hook.placeholders.MVdWPlaceholderAPIHook;
import me.dadus33.chatitem.hook.placeholders.OwnPlaceholder;
import me.dadus33.chatitem.hook.placeholders.PlaceholderAPIHook;
import me.dadus33.chatitem.hook.placeholders.VaultPlaceholderHook;
import me.dadus33.chatitem.invsee.InvShower;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.listeners.InventoryListener;
import me.dadus33.chatitem.listeners.JoinListener;
import me.dadus33.chatitem.listeners.TranslationInventoryListener;
import me.dadus33.chatitem.platform.IPlatform;
import me.dadus33.chatitem.platform.hook.PaperPlatform;
import me.dadus33.chatitem.platform.hook.SpigotPlatform;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.SemVer;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;

public class ChatItem extends JavaPlugin {

	private static final List<IPlaceholders> PLACEHOLDERS = new ArrayList<>();
	public static List<IPlaceholders> getPlaceholders() {
		return PLACEHOLDERS;
	}
	public static String replace(Player p, String text) {
		for(IPlaceholders ip : getPlaceholders())
			text = ip.replace(p, text);
		return text;
	}
	private static final IPlatform platform = Utils.IS_PAPER ? new PaperPlatform() : new SpigotPlatform();
	public static IPlatform getPlatform() {
		return platform;
	}
	public final static int CFG_VER = 13;
	public static boolean discordSrvSupport = false, hasNewVersion = false;
	private static ChatItem instance;
	private final String brandChannelName = Version.getVersion().isNewerOrEquals(Version.V1_13) ? "minecraft:brand" : "MC|Brand";
	private final List<ChatManager> chatManager = new ArrayList<>();
	private Storage storage;

	public static void reload(CommandSender sender) {
		ChatItem pl = getInstance();
		if (pl.getConfig().getKeys(false).isEmpty())
			pl.saveDefaultConfig();
		pl.reloadConfig();
		pl.storage = new Storage(pl.getConfig());
		//pl.chooseManagers();
		Translation.load(pl);
		InvShower.getInvShower().clear();
		if(sender != null) {
			if (!pl.storage.messageReload.isEmpty())
				sender.sendMessage(pl.storage.messageReload);
		}
		ChatManager.setTesting(null);
	}

	public static ChatItem getInstance() {
		return instance;
	}

	public static void debug(String msg) {
		if (getInstance().getStorage().debug)
			getInstance().getLogger().info("[Debug] " + msg.replace(ChatManager.SEPARATOR, 'S').replace(ChatManager.SEPARATOR_END, 'E'));
	}
	public static boolean hasDebug() {
		return getInstance().getStorage().debug;
	}

	private void chooseManagers() {
		this.chatManager.forEach((cm) -> cm.unload(this));
		this.chatManager.clear();
		
		this.chatManager.add(new PacketEditingChatManager(this));
		this.chatManager.add(new ChatListenerChatManager(this));
		if(Utils.IS_PAPER)
			this.chatManager.add(new PaperChatManager(this));
		
		String managerName = getStorage().manager;

		switch (managerName.toLowerCase(Locale.ROOT)) {
		case "both":
		case "all":
		case "auto":
			getLogger().info("Manager automatically chosen: " + getVisualChatManagers());
			break;
		case "paper":
		case "packet":
		case "chat":
			getLogger().info("Manager chosen: " + getVisualChatManagers());
			break;
		default:
			getLogger().severe("----- WARN -----");
			getLogger().severe("Failed to find manager: " + managerName + ".");
			getLogger().severe("Please reset your config and/or check wiki for more information");
			getLogger().severe("Using default manager: chat.");
			getLogger().severe("----- WARN -----");
			break;
		}

		this.chatManager.forEach((cm) -> cm.load(this, getStorage()));

		NamerManager.load(this);
		PlayerNamerManager.load(this);
	}

	public static List<String> getPluginThatRequirePacket() {
		return Arrays.asList("DeluxeChat", "HexNicks", "VentureChat", "ItemsAdder");
	}

	@Override
	public void onEnable() {
		// Save the instance (we're basically a singleton)
		instance = this;
		getLogger().info("Detected server version: " + Version.getVersion().name().toLowerCase() + " (Platform: " + getPlatform().getName() + ")");

		// Load config
		if (!new File(getDataFolder(), "config.yml").exists()) {
			getLogger().info("--------------");
			getLogger().info("");
			getLogger().info("Welcome on ChatItem !");
			getLogger().info("If you need help for setup, USE: /chatitem select");
			getLogger().info("Then follow steps.");
			getLogger().info("");
			getLogger().info("--------------");
			saveDefaultConfig();
		}
		storage = new Storage(getConfig());

		// Commands
		loadCommand(getCommand("cireload"), new CIReloadCommand());
		loadCommand(getCommand("chatitem"), new ChatItemCommand());

		// events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinListener(), this);
		pm.registerEvents(new InventoryListener(), this);
		pm.registerEvents(new TranslationInventoryListener(), this);
		pm.registerEvents(new ChatEventListener(), this);

		chooseManagers();

		StringJoiner plugins = new StringJoiner(", ");
		if (pm.isPluginEnabled("DiscordSRV")) {
			discordSrvSupport = true;
			plugins.add("DiscordSRV");
		}
		if (pm.isPluginEnabled("ChatControl") && ReflectionUtils.isClassExist("org.mineacademy.chatcontrol.settings.Settings")) {
			ChatControlSupport.init(this);
			plugins.add("ChatControl");
		}
		if (pm.isPluginEnabled("ChatManager") && ReflectionUtils.isClassExist("me.h1dd3nxn1nja.chatmanager.Main")) {
			ChatManagerSupport.init(this);
			plugins.add("ChatManager");
		}
		if (pm.isPluginEnabled("EcoEnchants") && EcoEnchantsSupport.load()) {
			plugins.add("EcoEnchants");
		}
		if (pm.isPluginEnabled("PlaceholderAPI")) {
			plugins.add("PlaceholderAPI");
			PLACEHOLDERS.add(new PlaceholderAPIHook());
		}
		if (pm.isPluginEnabled("MVdWPlaceholderAPI")) {
			plugins.add("MVdWPlaceholderAPI");
			PLACEHOLDERS.add(new MVdWPlaceholderAPIHook());
		}
		if (pm.isPluginEnabled("Vault")) {
			plugins.add("Vault");
			PLACEHOLDERS.add(new VaultPlaceholderHook());
		}

		if (plugins.length() > 0)
			getLogger().info("Load " + plugins.toString() + " support.");

		PLACEHOLDERS.add(new OwnPlaceholder());
		Translation.load(this);

		getServer().getMessenger().registerIncomingPluginChannel(this, brandChannelName, (chan, p, msg) -> ItemPlayer.getPlayer(p).setClientName(new String(msg).substring(1)));

		if (storage.checkUpdate) {
			CompletableFuture.runAsync(() -> {
				String urlName = "https://api.spigotmc.org/legacy/update.php?resource=19064";
				String content = Utils.getFromURL(urlName);
				if (Strings.isNullOrEmpty(content))
					return;
				SemVer currentVersion = SemVer.parse(getPlatform().getPluginVersion(this));
				if (currentVersion == null)
					return;
				SemVer latestVersion = SemVer.parse(content);
				if (latestVersion != null && latestVersion.isNewerThan(currentVersion)) {
					hasNewVersion = !content.equalsIgnoreCase(getPlatform().getPluginVersion(this));
					if (hasNewVersion)
						getLogger().info(storage.updateMessage);
				}
			});
		}
	}

	public void loadCommand(PluginCommand cmd, Object obj) {
		if (obj instanceof CommandExecutor)
			cmd.setExecutor((CommandExecutor) obj);
		if (obj instanceof TabCompleter)
			cmd.setTabCompleter((TabCompleter) obj);
	}

	public Storage getStorage() {
		return storage;
	}

	public List<ChatManager> getChatManager() {
		return chatManager;
	}
	
	public boolean isManagerEnabled(String id) {
		for (ChatManager cm : chatManager) {
			if(cm.getId().equalsIgnoreCase(id))
				return true;
		}
		return false;
	}

	public String getVisualChatManagers() {
		StringJoiner sj = new StringJoiner(", ");
		for (ChatManager cm : chatManager) {
			sj.add(cm.getName() + " (" + cm.getId() + ")");
		}
		return sj.toString();
	}

	public boolean isHasNewVersion() {
		return hasNewVersion;
	}
}
