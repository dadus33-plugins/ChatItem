package me.dadus33.chatitem.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.collect.ImmutableList;

import me.dadus33.chatitem.ChatItem;

public class Storage {

    public String handName, nameFormat, amountFormat, commandFormat, manager, permissionName, buggedClientAction, language;
    public final String messageNoPermission, messageDeny, messageReload, messageCooldown, messageLimit, messageIgnoredItem;
    public final String SECONDS, minutes, hours;
    public final String updateMessage, updateHover;
    public final List<String> placeholders, tooltipHand, tooltipBuggedClient, ignoredItems;
    public boolean colorIfColored, addAmountForced, letMessageThrough, denyIfNoItem, debug,
            handDisabled, showNoPermissionMessage, checkUpdate, permissionEnabled;
    public int configVersion, limit, cooldown;
    private FileConfiguration conf;

    public Storage(FileConfiguration cnf) {
        this.conf = cnf;
        configVersion = conf.getInt("config-version", 13);
        checkConfigVersion();
        this.manager = conf.getString("manager", "auto");
        language = conf.getString("general.language", "en_gb");
        debug = conf.getBoolean("debug", false);
        placeholders = ImmutableList.copyOf(conf.getStringList("general.placeholders"));
        nameFormat = color(conf.getString("general.name-format", "&b&l&o{name} {amount}&r"));
        amountFormat = color(conf.getString("general.amount-format", "x{times}"));
        commandFormat = color(conf.getString("general.command-format", "&6%name%'s item is %item%"));
        colorIfColored = conf.getBoolean("general.color-if-already-colored", true);
        letMessageThrough = conf.getBoolean("general.let-message-through", true);
        addAmountForced = conf.getBoolean("general.force-add-amount", true);
        denyIfNoItem = conf.getBoolean("general.deny-if-no-item", false);
        limit = conf.getInt("general.limit", 8);
        cooldown = conf.getInt("general.cooldown", 60);
        showNoPermissionMessage = conf.getBoolean("general.show-no-permission-message.normal", true);
        permissionEnabled = conf.getBoolean("general.permission.enabled", false);
        permissionName = conf.getString("general.permission.name", "chatitem.use");
        handDisabled = conf.getBoolean("general.hand.disabled", false);
        handName = color(conf.getString("general.hand.name", "&b&l&o{display-name}&b&l&o's hand"));
        tooltipHand = conf.getStringList("general.hand.tooltip").stream().map(Storage::color).collect(Collectors.toList());
        buggedClientAction = conf.getString("general.bugged_client.action", "show_both");
        tooltipBuggedClient = conf.getStringList("general.bugged_client.tooltip").stream().map(Storage::color).collect(Collectors.toList());
        ignoredItems = conf.getStringList("general.ignored_items");
        checkUpdate = conf.getBoolean("general.check-update", true);
        messageDeny = color(conf.getString("messages.deny-message", "&c&lYou have no item in hand!"));
        messageReload = color(conf.getString("messages.reload-success", "&b&lSuccessful reload!"));
        messageNoPermission = color(conf.getString("messages.no-permission",
                "&c&lI'm sorry, but you are not allowed to use the placeholder in chat!"));
        messageCooldown = color(conf.getString("messages.cooldown-message",
                "&c&lYou can only use items in chat once a minute! You have {remaining} left!"));
        updateMessage = color(conf.getString("messages.join-update.message",
                "&cA new version of ChatItem is available. &aClick here to download."));
        updateHover = color(conf.getString("messages.join-update.hover", "&6Click to go to spigot page !"));
        messageLimit = color(conf.getString("messages.limit-message", "&c&lYou can only add 8 item placeholders per message!"));
        messageIgnoredItem = color(conf.getString("messages.ignored-item", "&cThis item is not allowed to be showed."));

        SECONDS = color(conf.getString("messages.seconds", " seconds"));
        minutes = color(conf.getString("messages.minutes", " minutes"));
        hours = color(conf.getString("messages.hours", " hours"));
        colorStringList(tooltipHand);

    }

    public static String color(String s) {
        return s == null || s.isEmpty() ? s : ChatColor.translateAlternateColorCodes('&', s);
    }

    private static void colorStringList(List<String> input) {
        for (int i = 0; i < input.size(); ++i) {
            input.set(i, color(input.get(i)));
        }
    }

    private void checkConfigVersion() {
        int latestVersion = ChatItem.CFG_VER;
        if (latestVersion != configVersion) {
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

}
