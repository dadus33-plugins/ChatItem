package me.dadus33.chatitem;

import com.google.common.base.Strings;
import me.dadus33.chatitem.chatmanager.ChatManager;
import me.dadus33.chatitem.chatmanager.v1.PacketEditingChatManager;
import me.dadus33.chatitem.chatmanager.v2.ChatListenerChatManager;
import me.dadus33.chatitem.commands.CIReloadCommand;
import me.dadus33.chatitem.commands.ChatItemCommand;
import me.dadus33.chatitem.filters.Log4jFilter;
import me.dadus33.chatitem.itemnamer.NamerManager;
import me.dadus33.chatitem.listeners.InventoryListener;
import me.dadus33.chatitem.listeners.JoinListener;
import me.dadus33.chatitem.playernamer.PlayerNamerManager;
import me.dadus33.chatitem.utils.SemVer;
import me.dadus33.chatitem.utils.Storage;
import me.dadus33.chatitem.utils.Utils;
import me.dadus33.chatitem.utils.Version;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ChatItem extends JavaPlugin {

    public final static int CFG_VER = 13;
    public static boolean discordSrvSupport = false, hasNewVersion = false;
    private static ChatItem instance;
    private final String brandChannelName = Version.getVersion().isNewerOrEquals(Version.V1_13) ? "minecraft:brand" : "MC|Brand";
    private final List<ChatManager> chatManager = new ArrayList<>();
    private Log4jFilter filter;
    private Storage storage;

    public static void reload(CommandSender sender) {
        ChatItem pl = getInstance();
        if (pl.getConfig().getKeys(false).isEmpty())
            pl.saveDefaultConfig();
        pl.reloadConfig();
        String oldChatManager = pl.storage.MANAGER;
        pl.storage = new Storage(pl.getConfig());
        pl.chooseManagers();
        pl.filter.setStorage(pl.storage);
        if (!pl.storage.RELOAD_MESSAGE.isEmpty())
            sender.sendMessage(pl.storage.RELOAD_MESSAGE);
        if (!oldChatManager.equalsIgnoreCase(pl.storage.MANAGER))
            sender.sendMessage(ChatColor.GOLD + "Changing the manager with command reloading CAN produce issue. It's mostly suggested to restart after finding the better manager for you.");
    }

    public static ChatItem getInstance() {
        return instance;
    }

    public static void debug(String msg) {
        if (getInstance().getStorage().DEBUG)
            getInstance().getLogger().info("[Debug] " + msg);
    }

    private void chooseManagers() {
        this.chatManager.forEach((cm) -> cm.unload(this));
        this.chatManager.clear();
        PluginManager pm = getServer().getPluginManager();
        String managerName = getStorage().MANAGER;

        switch (managerName.toLowerCase(Locale.ROOT)) {
            case "both":
                this.chatManager.add(new PacketEditingChatManager(this));
                this.chatManager.add(new ChatListenerChatManager(this));
                getLogger().info("Manager automatically chosen: " + getVisualChatManagers());
            case "auto":
                if (getPluginThatRequirePacket().stream().map(pm::getPlugin).anyMatch(Objects::nonNull) && Version.getVersion().isNewerThan(Version.V1_7))
                    this.chatManager.add(new PacketEditingChatManager(this));
                else
                    this.chatManager.add(new ChatListenerChatManager(this));
                getLogger().info("Manager automatically chosen: " + getVisualChatManagers());
            case "packet":
                this.chatManager.add(new PacketEditingChatManager(this));
                getLogger().info("Manager chosen: " + getVisualChatManagers());
            case "chat":
                this.chatManager.add(new ChatListenerChatManager(this));
                getLogger().info("Manager chosen: " + getVisualChatManagers());
            default:
                getLogger().severe("----- WARN -----");
                getLogger().severe("Failed to find manager: " + managerName + ".");
                getLogger().severe("Please reset your config and/or check wiki for more information");
                getLogger().severe("Using default manager: chat.");
                getLogger().severe("----- WARN -----");
                this.chatManager.add(new ChatListenerChatManager(this));
        }

        this.chatManager.forEach((cm) -> cm.load(this, getStorage()));

        NamerManager.load(this);
        PlayerNamerManager.load(this);
    }

    private List<String> getPluginThatRequirePacket() {
        return Arrays.asList("DeluxeChat", "HexNicks", "VentureChat");
    }

    @Override
    public void onEnable() {
        //Save the instance (we're basically a singleton)
        instance = this;

        //Load config
        saveDefaultConfig();
        storage = new Storage(getConfig());

        //Commands
        loadCommand(getCommand("cireload"), new CIReloadCommand());
        loadCommand(getCommand("chatitem"), new ChatItemCommand());

        // events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new InventoryListener(), this);

        chooseManagers();

        //Initialize Log4J filter (remove ugly console messages)
        filter = new Log4jFilter(storage);

        if (pm.getPlugin("DiscordSRV") != null) {
            discordSrvSupport = true;
            getLogger().info("Load DiscordSRV support.");
        }

        getServer().getMessenger().registerIncomingPluginChannel(this, brandChannelName, (chan, p, msg) -> {
            String client = new String(msg);
            ChatItem.debug("Detected client " + client + " for " + p.getName());
            ItemPlayer.getPlayer(p).setClientName(client);
        });

        if (storage.CHECK_UPDATE) {
            getServer().getScheduler().runTaskAsynchronously(this, () -> {
                String urlName = "https://api.spigotmc.org/legacy/update.php?resource=19064";
                String content = Utils.getFromURL(urlName);
                if (Strings.isNullOrEmpty(content))
                    return;
                SemVer currentVersion = SemVer.parse(getDescription().getVersion());
                if (currentVersion == null)
                    return;
                SemVer latestVersion = SemVer.parse(content);
                if (latestVersion != null && latestVersion.isNewerThan(currentVersion)) {
                    hasNewVersion = !content.equalsIgnoreCase(getDescription().getVersion());
                    if (hasNewVersion) {
                        getLogger().info(storage.JOIN_UPDATE_MESSAGE);
                    }
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
