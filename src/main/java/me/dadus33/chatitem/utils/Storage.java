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

    public final Boolean DEBUG;
    public final Boolean COLOR_IF_ALREADY_COLORED;
    public final Boolean FORCE_ADD_AMOUNT;
    public final Boolean LET_MESSAGE_THROUGH;
    public final Boolean DENY_IF_NO_ITEM;
    public final Boolean HAND_DISABLED;
    public final String HAND_NAME;
    public final String NAME_FORMAT;
    public final String AMOUNT_FORMAT;
    public final String DENY_MESSAGE;
    public final String LIMIT_MESSAGE;
    public final String RELOAD_MESSAGE;
    public final String COOLDOWN_MESSAGE;
    public final String SECONDS;
    public final String MINUTES;
    public final String HOURS;
    private final Integer CONFIG_VERSION;
    public final Long COOLDOWN;
    public final Integer LIMIT;
    public final List<Command> ALLOWED_PLUGIN_COMMANDS = new ArrayList<>();
    public final List<String> ALLOWED_DEFAULT_COMMANDS = new ArrayList<>();
    public final List<String> PLACEHOLDERS;
    public final List<String> HAND_TOOLTIP;

    public Storage(FileConfiguration cnf) {
        this.conf = cnf;
        CONFIG_VERSION = conf.getInt("config-version");
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
        COOLDOWN = conf.getLong("General.cooldown");
        LIMIT = conf.getInt("General.limit");
        List<String> added = conf.getStringList("General.placeholders");
        PLACEHOLDERS = ImmutableList.copyOf(added);
        NAME_FORMAT = color(conf.getString("General.name-format"));
        AMOUNT_FORMAT = color(conf.getString("General.amount-format"));
        COLOR_IF_ALREADY_COLORED = conf.getBoolean("General.color-if-already-colored");
        LET_MESSAGE_THROUGH = conf.getBoolean("General.let-message-through");
        FORCE_ADD_AMOUNT = conf.getBoolean("General.force-add-amount");
        DENY_IF_NO_ITEM = conf.getBoolean("General.deny-if-no-item");
        HAND_DISABLED = conf.getBoolean("General.hand.disabled");
        DEBUG = conf.getBoolean("debug");
        DENY_MESSAGE = color(conf.getString("Messages.deny-message"));
        HAND_NAME = color(conf.getString("General.hand.name"));
        LIMIT_MESSAGE = color(conf.getString("Messages.limit-message"));
        RELOAD_MESSAGE = color(conf.getString("Messages.reload-success"));
        COOLDOWN_MESSAGE = color(conf.getString("Messages.cooldown-message"));
        SECONDS = color(conf.getString("Messages.seconds"));
        MINUTES = color(conf.getString("Messages.minutes"));
        HOURS = color(conf.getString("Messages.hours"));
        HAND_TOOLTIP = conf.getStringList("General.hand.tooltip");
        colorStringList(HAND_TOOLTIP);
        final List<String> cmds = conf.getStringList("General.commands");
        Bukkit.getScheduler().runTaskLaterAsynchronously(ChatItem.getInstance(), new Runnable() {
            @Override
            public void run() {
                for(String s : cmds){
                    Command c = Bukkit.getPluginCommand(s);
                    if(c!=null) {
                        ALLOWED_PLUGIN_COMMANDS.add(c);
                    }
                    else {
                        ALLOWED_DEFAULT_COMMANDS.add(s);
                    }
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
