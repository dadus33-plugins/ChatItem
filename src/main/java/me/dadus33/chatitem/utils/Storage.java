package me.dadus33.chatitem.utils;

import com.google.common.collect.ImmutableList;
import me.dadus33.chatitem.ChatItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;


public class Storage {

    private FileConfiguration conf;
    public final HashMap<String, HashMap<Short, String>> TRANSLATIONS = new HashMap<>();
    public final boolean COLOR_IF_ALREADY_COLORED, FORCE_ADD_AMOUNT, LET_MESSAGE_THROUGH, DENY_IF_NO_ITEM, HAND_DISABLED, SHOW_NO_PERM_NORMAL, CHECK_UPDATE;
    public final String HAND_NAME, NAME_FORMAT, AMOUNT_FORMAT;
    public final String NO_PERMISSION_MESSAGE, DENY_MESSAGE, RELOAD_MESSAGE, COOLDOWN_MESSAGE;
    public final String SECONDS, MINUTES, HOURS;
    public final String JOIN_UPDATE_MESSAGE, JOIN_UPDATE_HOVER;
    private final int CONFIG_VERSION;
    public final long COOLDOWN;
    public final List<Command> ALLOWED_PLUGIN_COMMANDS = new ArrayList<>();
    public final List<String> ALLOWED_DEFAULT_COMMANDS = new ArrayList<>();
    public final List<String> PLACEHOLDERS;
    public final List<String> HAND_TOOLTIP;

    public Storage(FileConfiguration cnf) {
        this.conf = cnf;
        CONFIG_VERSION = conf.getInt("config-version", 12);
        checkConfigVersion();
        Set<String> keys = conf.getConfigurationSection("Translations").getKeys(false);
        for (String key : keys) {
            HashMap<Short, String> entry = new HashMap<>();
            Set<String> subKeys = conf.getConfigurationSection("Translations.".concat(key)).getKeys(false);
            for (String subKey : subKeys) {
                entry.put(Short.parseShort(subKey), color(conf.getString("Translations.".concat(key).concat(".")
                        .concat(subKey))));
            }

            TRANSLATIONS.put(key, entry);
        }
        PLACEHOLDERS = ImmutableList.copyOf(conf.getStringList("General.placeholders"));
        NAME_FORMAT = color(conf.getString("General.name-format", "&b&l&o{name} {amount}&r"));
        AMOUNT_FORMAT = color(conf.getString("General.amount-format", "x{times}"));
        COLOR_IF_ALREADY_COLORED = conf.getBoolean("General.color-if-already-colored", true);
        LET_MESSAGE_THROUGH = conf.getBoolean("General.let-message-through", true);
        FORCE_ADD_AMOUNT = conf.getBoolean("General.force-add-amount", true);
        DENY_IF_NO_ITEM = conf.getBoolean("General.deny-if-no-item", false);
        COOLDOWN = conf.getLong("General.cooldown", 60);
        SHOW_NO_PERM_NORMAL = conf.getBoolean("General.show-no-permission-message.normal", true);
        HAND_DISABLED = conf.getBoolean("General.hand.disabled", false);
        HAND_NAME = color(conf.getString("General.hand.name", "&b&l&o{display-name}&b&l&o's hand"));
        HAND_TOOLTIP = conf.getStringList("General.hand.tooltip");
        CHECK_UPDATE = conf.getBoolean("General.check-update", true);
        DENY_MESSAGE = color(conf.getString("Messages.deny-message", "&c&lYou have no item in hand!"));
        RELOAD_MESSAGE = color(conf.getString("Messages.reload-success", "&b&lSuccessful reload!"));
        NO_PERMISSION_MESSAGE = color(conf.getString("Messages.no-permission", "&c&lI'm sorry, but you are not allowed to use the placeholder in chat!"));
        COOLDOWN_MESSAGE = color(conf.getString("Messages.cooldown-message", "&c&lYou can only use items in chat once a minute! You have {remaining} left!"));
        JOIN_UPDATE_MESSAGE = color(conf.getString("Messages.join-update.message", "&cA new version of ChatItem is available. &aClick here to download."));
        JOIN_UPDATE_HOVER = color(conf.getString("Messages.join-update.hover", "&6Click to go to spigot page !"));
        
        SECONDS = color(conf.getString("Messages.seconds", " seconds"));
        MINUTES = color(conf.getString("Messages.minutes", " minutes"));
        HOURS = color(conf.getString("Messages.hours", " hours"));
        colorStringList(HAND_TOOLTIP);
        final List<String> cmds = conf.getStringList("General.commands");
        Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), () -> {
            for(String s : cmds){
                Command c = Bukkit.getPluginCommand(s);
                if(c != null) {
                    ALLOWED_PLUGIN_COMMANDS.add(c);
                } else {
                    ALLOWED_DEFAULT_COMMANDS.add(s);
                }
            }
        }, 100L);

    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private void checkConfigVersion() {
        int latestVersion = ChatItem.CFG_VER;
        if (latestVersion != CONFIG_VERSION) {
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "ChatItem detected an older or invalid configuration file. Replacing it with the default config...");
            performOverwrite();
            conf = ChatItem.getInstance().getConfig();
            Bukkit.getLogger().log(Level.WARNING, ChatColor.RED + "Replacement complete!");
        }
    }


    private void performOverwrite() {
        ChatItem.getInstance().saveResource("config.yml", true);
    }

    private static void colorStringList(List<String> input){
        for(int i=0; i<input.size(); ++i){
            input.set(i, color(input.get(i)));
        }
    }

}
